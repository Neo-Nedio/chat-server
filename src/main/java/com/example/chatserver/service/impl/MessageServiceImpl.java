package com.example.chatserver.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.admin.vo.expose.ThirdSendMsgVo;
import com.example.chatserver.config.VoiceConfig;
import com.example.chatserver.constant.MessageContentType;
import com.example.chatserver.constant.MsgSource;
import com.example.chatserver.constant.MsgType;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.Top10MsgDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.MessageRetraction;
import com.example.chatserver.entity.User;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.MessageMapper;
import com.example.chatserver.service.*;
import com.example.chatserver.utils.FileUtil;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.vo.message.MessageRecordVo;
import com.example.chatserver.vo.message.ReeditMsgVo;
import com.example.chatserver.vo.message.RetractionMsgVo;
import com.example.chatserver.websocket.WebSocketService;
import com.example.chatserver.vo.message.SendMsgVo;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    FriendService friendService;

    @Resource
    WebSocketService webSocketService;

    @Resource
    ChatListService chatListService;

    @Resource
    MessageMapper messageMapper;

    @Resource
    MessageRetractionService messageRetractionService;

    @Resource
    UserService userService;

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    MQProducerService mqProducerService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RestTemplate restTemplate;

    @Resource
    VoiceConfig voiceConfig;

    private Message getMessage(String userId, MsgContent msgContent, String source, String type, String toUserId) {
        //获取上一条显示时间的消息
        Message previousMessage = messageMapper.getPreviousShowTimeMsg(userId, toUserId);
        //存入数据库
        Message message = new Message();
        message.setId(IdUtil.randomUUID());
        message.setFromId(userId);
        message.setSource(source);
        message.setToId(toUserId);
        message.setType(type);
        //超过五分钟显示时间
        if (null == previousMessage) {
            message.setIsShowTime(true);
        } else {
            message.setIsShowTime(DateUtil.between(new Date(), previousMessage.getUpdateTime(), DateUnit.MINUTE) > 5);
        }
        if (MessageContentType.Img.equals(msgContent.getType()) ||
                MessageContentType.File.equals(msgContent.getType()) ||
                MessageContentType.Voice.equals(msgContent.getType())) {
            JSONObject content = JSONUtil.parseObj(msgContent.getContent());
            String name = (String) content.get("name");
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            String fileName = userId + "/" + toUserId + "/" + IdUtil.randomUUID() + "." + fileType;
            content.set("fileName", fileName);
            content.set("url", minioUtil.getUrl(fileName));
            content.set("type", fileType);
            msgContent.setContent(content.toJSONString(0));
        }
        message.setMsgContent(msgContent);
        return message;
    }

    //第二个参数为toUserId
    public Message sendMessage(String userId, String toUserId, MsgContent msgContent, String source, String type) {
        Message message = getMessage(userId, msgContent, source, type, toUserId);
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    //第二个参数为SendMsgVo，用于查看是否是转发消息
    public Message sendMessage(String userId, SendMsgVo sendMsgVo,MsgContent msgContent ,String source, String type) {
        final String toUserId = sendMsgVo.getToUserId();
        Message message = getMessage(userId, msgContent, source, type, toUserId);

        //是转发消息
        if (null != sendMsgVo.getIsForward() && sendMsgVo.getIsForward())
            message.setFromForwardMsgId(sendMsgVo.getFromMsgId());
        boolean isSave = save(message);
        if (isSave) return message;
        return null;
    }

    //给用户发送消息
    public Message sendMessageToUser(String userId, SendMsgVo sendMsgVo, String type) {
        //验证是否是好友
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, sendMsgVo.getToUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        Message message = sendMessage(userId, sendMsgVo, sendMsgVo.getMsgContent(), MsgSource.User, type);
        MsgContent msgContent = message.getMsgContent();
        FriendDetailsDto friendDetails = friendService.getFriendDetails(sendMsgVo.getToUserId(), userId);
        msgContent.setFromUserId(userId);
        msgContent.setFromUserName(StringUtils.isNotBlank(friendDetails.getRemark())
                ? friendDetails.getRemark() : friendDetails.getName());
        msgContent.setFromUserPortrait(friendDetails.getPortrait());
        //更新聊天列表（展示名与头像已在 sendMessage 内按接收方视角写入 msgContent）
        chatListService.updateChatList(message.getToId(), userId, message.getMsgContent(), MsgSource.User);
        try {
            //发送消息
            mqProducerService.sendMsgToUser(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToUser(message, message.getToId());
        }
        return message;

    }

    //给群聊发送消息
    public Message sendMessageToGroup(String userId, SendMsgVo sendMsgVo, String type) {
        Message message = sendMessage(userId, sendMsgVo, sendMsgVo.getMsgContent(), MsgSource.Group, type);
        //更新聊天列表
        chatListService.updateChatListGroup(message.getToId(), message.getMsgContent());
        try {
            mqProducerService.sendMsgToGroup(message);
        } catch (Exception e) {
            //发送消息
            webSocketService.sendMsgToGroup(message, message.getToId());
        }
        return message;
    }

    //根据发送目标选择用户还是群聊
    @Override
    public Message sendMessage(String userId, String role, SendMsgVo sendMsgVo, String type) {
        if (MsgSource.Group.equals(sendMsgVo.getSource())) {
            return sendMessageToGroup(userId, sendMsgVo, type);
        } else {
            return sendMessageToUser(userId,sendMsgVo, type);
        }
    }

    @Override
    public List<Message> messageRecord(String userId, MessageRecordVo messageRecordVo) {
        return messageMapper.messageRecord(userId, messageRecordVo.getTargetId(),
                messageRecordVo.getIndex(), messageRecordVo.getNum());
    }

    @Override
    public List<Message> messageRecordDesc(String userId, MessageRecordVo messageRecordVo) {
        return messageMapper.messageRecordDesc(userId, messageRecordVo.getTargetId(),
                messageRecordVo.getIndex(), messageRecordVo.getNum());
    }

    @Override
    public Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo) {
        MsgContent msgContent = new MsgContent();
        msgContent.setContent(fileInfo.toJSONString(0));
        msgContent.setType(MessageContentType.File);
        return sendMessage(userId, toUserId, msgContent, MsgSource.User, MsgType.User);
    }

    @Override
    public MsgContent getFileMsgContent(String userId, String msgId) {
        Message msg = getById(msgId);
        if (msg == null) {
            throw new BaseException("消息为空");
        }
        if (msg.getFromId().equals(userId) || msg.getToId().equals(userId)
                || chatGroupMemberService.isMemberExists(msg.getToId(), userId)) {
            return msg.getMsgContent();
        } else {
            throw new BaseException("消息为空");
        }
    }

    @Override
    public boolean updateMsgContent(String msgId, MsgContent msgContent) {
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Message::getMsgContent, msgContent)
                .eq(Message::getId, msgId);
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Message retractionMsg(String userId, RetractionMsgVo retractionMsgVo) {
        Message message = getById(retractionMsgVo.getMsgId());
        if (null == message)
            throw new BaseException("消息不存在");
        if (!userId.equals(message.getFromId())) {
            throw new BaseException("只能撤回自己发送的消息");
        }
        if (message.getUpdateTime() == null
                || DateUtil.between(message.getUpdateTime(), new Date(), DateUnit.SECOND) >= 180) {
            throw new BaseException("发送超过三分钟的消息不能撤回");
        }

        MsgContent msgContent = message.getMsgContent();
        //如果是私聊消息，把对方给撤回方的备注写上
        if (MsgSource.User.equals(message.getSource())) {
            FriendDetailsDto friendDetails = friendService.getFriendDetails(message.getToId(), userId);
            msgContent.setFromUserName(StringUtils.isNotBlank(friendDetails.getRemark())
                    ? friendDetails.getRemark() : friendDetails.getName());
        }

        msgContent.setExt(msgContent.getType());
        //只有文本才保存，之前的消息内容
        if (MessageContentType.Text.equals(msgContent.getType())) {
            MessageRetraction messageRetraction = new MessageRetraction();
            messageRetraction.setId(IdUtil.randomUUID());
            messageRetraction.setMsgId(message.getId());
            messageRetraction.setMsgContent(msgContent);
            messageRetractionService.save(messageRetraction);

        }

        //把信息内容设置为撤销
        msgContent.setType(MessageContentType.Retraction);
        msgContent.setContent("");
        updateById(message);

        //更新发送方的聊天列表
        ChatList userIdchatList = chatListService.getChatListByUserIdAndFromId(userId, message.getToId());
        userIdchatList.setLastMsgContent(msgContent);
        chatListService.updateById(userIdchatList);

        //更新接收方的聊天列表
        ChatList toIdchatList;
        if (MsgSource.User.equals(message.getSource())) {
            // 单聊：对方是接收方
            toIdchatList = chatListService.getChatListByUserIdAndFromId(message.getToId(), userId);
        } else {
            // 群聊：toId 是群ID
            toIdchatList = chatListService.getChatListByUserIdAndFromId(message.getFromId(), message.getToId());
        }
        toIdchatList.setLastMsgContent(msgContent);
        chatListService.updateById(toIdchatList);

        //发送
        if (message.getSource().equals(MsgSource.User))
            webSocketService.sendMsgToUser(message, message.getToId());
        if (message.getSource().equals(MsgSource.Group))
            webSocketService.sendMsgToGroup(message, retractionMsgVo.getTargetId());
        return message;
    }

    @Override
    //获取撤销前的内容
    public MessageRetraction reeditMsg(String userId, ReeditMsgVo reeditMsgVo) {
        LambdaQueryWrapper<MessageRetraction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageRetraction::getMsgId, reeditMsgVo.getMsgId());
        return messageRetractionService.getOne(queryWrapper);
    }

    @Override
    public String sendFileOrImg(String userId, String msgId, InputStream inputStream) throws IOException {
        MsgContent msgContent = getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        return minioUtil.uploadFile(inputStream, fileInfo.get("fileName").toString(), fileInfo.getLong("size"));
    }

    private @NotNull Message getVoiceMessage(Message message) {
        //检查是否已转换过
        JSONObject voice = JSONUtil.parseObj(message.getMsgContent().getContent());
        if (voice.containsKey("text")) {
            return message;
        }
        //获取语音的路径
        String fileName = voice.get("fileName").toString();
        try {
            // 从 MinIO 获取文件
            InputStream inputStream = minioUtil.getObject(fileName);
            byte[] content = IOUtils.toByteArray(inputStream);
            //包装成带文件名的 ByteArrayResource
            ByteArrayResource fileResource = FileUtil.createByteArrayResource(content, fileName);

            //构建 HTTP 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA); //内容类型，用于文件上传
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(); //可以存储多个值（文件 + 模型参数）
            body.add("file", fileResource); //添加文件参数
            body.add("model", voiceConfig.getModel()); //添加模型参数

            //包装请求体和请求头
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            //调用语音识别 API
            ResponseEntity<String> response = restTemplate.postForEntity(
                    voiceConfig.getTransitionApi(),
                    requestEntity,
                    String.class);

            //处理响应并保存结果
            JSONObject result = JSONUtil.parseObj(response.getBody());
            if (result.containsKey("text")) {
                String text = result.get("text").toString();
                voice.set("text", text);
                message.getMsgContent().setContent(voice.toJSONString(0));
                updateById(message);
                return message;
            } else {
                throw new BaseException("语音转换错误~");
            }
        } catch (Exception e) {
            log.error("voiceToText:{}", e.getMessage());
            throw new BaseException("语音转换错误~");
        }
    }

    @Override
    public Message voiceToText(String userId, String msgId) {
        Message message = getById(msgId);
        if (null == message || !MessageContentType.Voice.equals(message.getMsgContent().getType())) {
            throw new BaseException("这不是一条语音~");
        }
        //两个都不满足，说明既不是发送方也不是接收方，抛出异常
        if (!message.getToId().equals(userId) && !message.getFromId().equals(userId)) {
            throw new BaseException("不能查看其他~");
        }
        return getVoiceMessage(message);
    }

    @Override
    public Message voiceToText(String userId, String msgId,Boolean isChatGroupMessage) {
        Message message = getById(msgId);
        if (null == message || !MessageContentType.Voice.equals(message.getMsgContent().getType())) {
            throw new BaseException("这不是一条语音~");
        }
        //三个都不满足，说明既不是发送方也不是接收方，还不在群聊中，抛出异常
        if (!message.getToId().equals(userId) && !message.getFromId().equals(userId) && !isChatGroupMessage) {
            throw new BaseException("不能查看其他~");
        }
        return getVoiceMessage(message);
    }

    @Override
    public Integer messageNum(DateTime date) {
        return messageMapper.messageNum(date);
    }

    @Override
    public List<Top10MsgDto> getTop10Msg(Date date) {
        return messageMapper.getTop10Msg(date);
    }

    @Override
    public boolean thirdPartySendMsg(String userId, ThirdSendMsgVo thirdSendMsgVo) {
        if (userId == null)
            return false;
        User user = userService.getUserByEmail(thirdSendMsgVo.getEmail());


        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Text);
        msgContent.setContent(thirdSendMsgVo.getContent());

        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setMsgContent(msgContent);
        sendMsgVo.setToUserId(user.getId());
        sendMsgVo.setSource(MsgSource.User);
        sendMessageToUser(userId, sendMsgVo, MsgType.User);
        return true;
    }
}
