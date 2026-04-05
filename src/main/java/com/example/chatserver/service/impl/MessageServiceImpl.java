package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.MessageMapper;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.service.WebSocketService;
import com.example.chatserver.vo.message.SendMsgToUserVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    FriendService friendService;

    @Resource
    WebSocketService webSocketService;

    @Resource
    ChatListService chatListService;

    @Override
    public boolean sendMessageToUser(String userId, SendMsgToUserVo sendMsgToUserVo) {
        String toUserId = sendMsgToUserVo.getToUserId();
        //验证是否是好友
        boolean isFriend = friendService.isFriend(userId, toUserId);
        if (isFriend) {
            throw new BaseException("双方非好友");
        }
        //存入数据库
        Message message = new Message();
        message.setId(IdUtil.randomUUID());
        message.setFromId(userId);
        message.setToId(toUserId);
        message.setIsShowTime(sendMsgToUserVo.isShowTime());
        MsgContent msgContent = sendMsgToUserVo.getMsgContent();
        msgContent.setFromUserId(userId);
        message.setMsgContent(msgContent);
        boolean isSave = save(message); //保存信息
        if (isSave) {
            //发送消息
            webSocketService.sendToUser(message, toUserId);
            //更新聊天列表
            chatListService.updateChatList(toUserId, userId, msgContent);
            return true;
        }
        return false;
    }
}
