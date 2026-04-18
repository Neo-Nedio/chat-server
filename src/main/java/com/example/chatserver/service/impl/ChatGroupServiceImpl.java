package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.config.MinioConfig;
import com.example.chatserver.constant.*;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.dto.SystemMsgDto;
import com.example.chatserver.entity.*;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ChatGroupMapper;
import com.example.chatserver.mapper.ChatGroupNoticeMapper;
import com.example.chatserver.service.*;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.vo.chatGroup.*;
import com.example.chatserver.vo.message.SendMsgVo;
import com.example.chatserver.websocket.WebSocketService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements ChatGroupService {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    MessageService messageService;

    @Resource
    UserService userService;

    @Resource
    ChatGroupMapper chatGroupMapper;

    @Resource
    ChatGroupNoticeMapper chatGroupNoticeMapper;

    @Resource
    MinioConfig minioConfig;

    @Resource
    RedisUtils redisUtils;

    @Lazy
    @Resource
    NotifyService notifyService;

    @Resource
    WebSocketService webSocketService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo) {
        ///创建群聊
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(IdUtil.randomUUID());
        //设置群号
        Random random = new Random();
        String randomString = String.format("%010d", random.nextInt(1000000000)); //格式化为 10 位数字（不足 10 位前面补空格）
        LambdaQueryWrapper<ChatGroup> wrapper = new LambdaQueryWrapper<ChatGroup>()
                .eq(ChatGroup::getChatGroupNumber, randomString);
        while (count(wrapper) > 0){ //查询数据库是否存在这个群号
            //如果已存在，重新生成，直到找到不存在的
            randomString = String.format("%010d", random.nextInt(1000000000));
            wrapper.clear();
            wrapper.eq(ChatGroup::getChatGroupNumber, randomString);
        }
        chatGroup.setChatGroupNumber(randomString);

        chatGroup.setName(createChatGroupVo.getName());
        chatGroup.setMemberNum(Optional.ofNullable(createChatGroupVo.getUsers()).map(ArrayList::size).orElse(0) + 1 );
        chatGroup.setUserId(userId);
        chatGroup.setOwnerUserId(userId);
        chatGroup.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-group-portrait.png");
        if (createChatGroupVo.getNotice() != null){
            ChatGroupNotice chatGroupNotice = new ChatGroupNotice();
            chatGroupNotice.setId(IdUtil.randomUUID());
            chatGroupNotice.setChatGroupId(chatGroup.getId());
            chatGroupNotice.setNoticeContent(createChatGroupVo.getNotice());
            chatGroupNotice.setUserId(userId);
            chatGroupNoticeMapper.insert(chatGroupNotice);
            chatGroup.setNotice(chatGroupNotice);
        }
        boolean isSava = save(chatGroup);

        ///添加自己
        ChatGroupMember chatGroupMember = new ChatGroupMember();
        chatGroupMember.setId(IdUtil.randomUUID());
        chatGroupMember.setChatGroupId(chatGroup.getId());
        chatGroupMember.setUserId(userId);
        chatGroupMemberService.save(chatGroupMember);
        ///绑定群成员
        if (isSava && null != createChatGroupVo.getUsers()) {
            for (CreateChatGroupVo.User user : createChatGroupVo.getUsers()) {
                chatGroupMember = new ChatGroupMember();
                chatGroupMember.setId(IdUtil.randomUUID());
                chatGroupMember.setChatGroupId(chatGroup.getId());
                chatGroupMember.setUserId(user.getUserId());
                chatGroupMemberService.save(chatGroupMember);
            }
        }
        return isSava;
    }

    @Override
    public List<ChatGroup> searchGroup(String userId,String search) {
        return chatGroupMapper.searchGroup(userId,search);
    }

    @Override
    public List<ChatGroup> chatGroupList(String userId) {
        return chatGroupMapper.getList(userId);
    }

    @Override
    public ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupVo detailsChatGroupVo) {
        return chatGroupMapper.detailsChatGroup(userId, detailsChatGroupVo.getChatGroupId());
    }

    @Override
    public boolean isOwner(String groupId, String userId) {
        ChatGroup group = getById(groupId);
        return group.getOwnerUserId().equals(userId);
    }

    @Override
    public String getOwnerUserId(String groupId) {
        ChatGroup group = getById(groupId);
        if (group == null) {
            throw new BaseException("群聊不存在");
        }
        if( group.getStatus().equals(GroupStatus.Disable)){
            throw new BaseException("群聊已解散");
        }
        return group.getOwnerUserId();
    }

    @Override
    //头像从userController获取
    public boolean updateGroupPortrait(String groupId, String fileName) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getPortrait, fileName)
                .eq(ChatGroup::getId, groupId);
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateChatGroupName(String userId, UpdateChatGroupNameVo updateChatGroupNameVo) {
        if (!isOwner(updateChatGroupNameVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getName, updateChatGroupNameVo.getName())
                .eq(ChatGroup::getId, updateChatGroupNameVo.getGroupId());

        //发送群消息系统消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(updateChatGroupNameVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.System);
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent("群主")
                .addContent("修改了群名称");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(userId);
        msgContent.setExt(userId);
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        return update(updateWrapper);
    }

    @Override
    public boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo) {
        UpdateWrapper<ChatGroupMember> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(updateChatGroupVo.getUpdateKey(), updateChatGroupVo.getUpdateValue())
                .eq("chat_group_id", updateChatGroupVo.getGroupId())
                .eq("user_id", userId);
        return chatGroupMemberService.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean inviteMember(String userId, InviteMemberVo inviteMemberVo) {
        List<ChatGroupMember> members = new ArrayList<>();
        for (String inviteUserid : inviteMemberVo.getUserIds()) {
            //邀请
            if (chatGroupMemberService.isMemberExists(inviteMemberVo.getGroupId(), inviteUserid)) {
                continue;
            }
            ChatGroupMember member = new ChatGroupMember();
            member.setId(IdUtil.randomUUID());
            member.setUserId(inviteUserid);
            member.setChatGroupId(inviteMemberVo.getGroupId());
            members.add(member);

            //发送群消息系统消息
            SendMsgVo sendMsgVo = new SendMsgVo();
            sendMsgVo.setSource(MsgSource.Group);
            sendMsgVo.setToUserId(inviteMemberVo.getGroupId());
            MsgContent msgContent = new MsgContent();
            msgContent.setType(MessageContentType.System);
            User user = userService.getById(userId);
            User inviteUser = userService.getById(inviteUserid);
            //设置系统消息
            SystemMsgDto systemMsgDto = new SystemMsgDto();
            systemMsgDto.addEmphasizeContent(user.getName())
                    .addContent("邀请了")
                    .addEmphasizeContent(inviteUser.getName())
                    .addContent("加入了该群");
            msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
            msgContent.setFromUserId(userId);
            msgContent.setExt(userId);
            sendMsgVo.setMsgContent(msgContent);
            messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);
        }
        if (!members.isEmpty()) {
            ChatGroup chatGroup = getById(inviteMemberVo.getGroupId());
            chatGroup.setMemberNum(chatGroup.getMemberNum() + members.size());
            updateById(chatGroup);
            boolean ok = chatGroupMemberService.saveBatch(members);
            if (ok) {
                for (ChatGroupMember m : members) {
                    redisUtils.del("member:" + inviteMemberVo.getGroupId() + ":" + m.getUserId());
                }
            }
            return ok;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo) {
        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(quitChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(userId);
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent("退出了群聊");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(userId);
        msgContent.setExt(userId);
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        //从群聊中移出
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, quitChatGroupVo.getGroupId());
        chatGroupMemberService.remove(queryWrapper);
        redisUtils.del("member:" + quitChatGroupVo.getGroupId() + ":" + userId);

        //群聊更新
        ChatGroup chatGroup = getById(quitChatGroupVo.getGroupId());
        chatGroup.setMemberNum(chatGroup.getMemberNum() - 1);
        return updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean kickChatGroup(String userId, KickChatGroupVo kickChatGroupVo) {
        if (!isOwner(kickChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        if(userId.equals(kickChatGroupVo.getUserId())){
            throw new BaseException("不能踢出自己~");
        }

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(kickChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(kickChatGroupVo.getUserId());
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent("已被踢出该群");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(userId);
        msgContent.setExt(kickChatGroupVo.getUserId());
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        //踢出群成员
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, kickChatGroupVo.getGroupId())
                .eq(ChatGroupMember::getUserId, kickChatGroupVo.getUserId());
        chatGroupMemberService.remove(queryWrapper);
        redisUtils.del("member:" + kickChatGroupVo.getGroupId() + ":" + kickChatGroupVo.getUserId());

        //群成员减一
        ChatGroup chatGroup = getById(kickChatGroupVo.getGroupId());
        chatGroup.setMemberNum(chatGroup.getMemberNum() - 1);
        return updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean dissolveChatGroup(String userId, DissolveChatGroupVo dissolveChatGroupVo) {
        if (!isOwner(dissolveChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
/*
        // 先查询获取成员列表
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, dissolveChatGroupVo.getGroupId());
        List<ChatGroupMember> memberList = chatGroupMemberService.list(queryWrapper);
        // 踢出所有成员
        chatGroupMemberService.remove(queryWrapper);
        // 批量删除每个人的会话（优化版）
        if (memberList != null && !memberList.isEmpty()) {
            // 构建删除条件：群组ID = 会话目标ID，且用户ID在成员列表中
            LambdaQueryWrapper<ChatList> chatListLambdaQueryWrapper = new LambdaQueryWrapper<>();
            chatListLambdaQueryWrapper
                    .eq(ChatList::getFromId, dissolveChatGroupVo.getGroupId())
                    .in(ChatList::getUserId, memberList.stream()
                            .map(ChatGroupMember::getUserId)
                            .collect(Collectors.toList()));
            chatListService.remove(chatListLambdaQueryWrapper);
        }*/

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(dissolveChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        msgContent.setFromUserId(userId);
        msgContent.setExt("all"); //全部人被踢出，就是解散群聊
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent("该群已解散");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        //解散群聊
        ChatGroup updateGroup = new ChatGroup();
        updateGroup.setId(dissolveChatGroupVo.getGroupId());
        updateGroup.setStatus(GroupStatus.Disable);  // 0-已解散
        boolean ok = updateById(updateGroup);

        if (ok) {
            redisUtils.del("group-dissolved:" + dissolveChatGroupVo.getGroupId());
            //群已解散，相关通知不再需要任何人审批，未读方清空
            LambdaUpdateWrapper<Notify> notifyUpdate = new LambdaUpdateWrapper<>();
            notifyUpdate.set(Notify::getUnreadId, "")
                    .set(Notify::getStatus, FriendApplyStatus.Reject) //把待处理的消息在群解释后全改成拒绝
                    .eq(Notify::getToId, dissolveChatGroupVo.getGroupId())
                    .eq(Notify::getType, NotifyType.Group_Apply)
                    .eq(Notify::getStatus, FriendApplyStatus.Wait); //待处理的才修改
            notifyService.update(notifyUpdate);
        }
        return ok;
    }

    @Override
    public boolean isDissolveChatGroup(DissolveChatGroupVo dissolveChatGroupVo) {
        String chatGroupId = dissolveChatGroupVo.getGroupId();

        // 判断是否存在已解散的群聊
        return chatGroupMapper.exists(
                new LambdaQueryWrapper<ChatGroup>()
                        .eq(ChatGroup::getId, chatGroupId)
                        .eq(ChatGroup::getStatus, GroupStatus.Disable)
        );
    }
    @Override
    public boolean transferChatGroup(String userId, TransferChatGroupVo transferChatGroupVo) {
        if (!isOwner(transferChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");

        //发送群消息系统消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(transferChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Transfer);
        User user = userService.getById(userId);
        User inviteUser = userService.getById(transferChatGroupVo.getUserId());
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent("把群主转让给了")
                .addEmphasizeContent(inviteUser.getName());
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(userId);
        msgContent.setExt(userId);
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        //更换群主
        ChatGroup chatGroup = getById(transferChatGroupVo.getGroupId());
        chatGroup.setOwnerUserId(transferChatGroupVo.getUserId());
        boolean ok = updateById(chatGroup);

        if (ok) {
            //群主已变更，相关待审批通知（未处理）的未读方改为新群主
            LambdaUpdateWrapper<Notify> notifyUpdate = new LambdaUpdateWrapper<>();
            notifyUpdate.set(Notify::getUnreadId, transferChatGroupVo.getUserId())
                    .eq(Notify::getToId, transferChatGroupVo.getGroupId())
                    .eq(Notify::getType, NotifyType.Group_Apply)
                    .eq(Notify::getStatus, FriendApplyStatus.Wait) //待处理的才修改，否则处理后UnreadId回变成申请人，他没读就回变成新的群主
                    .isNotNull(Notify::getUnreadId) //添加 WHERE 条件，不为空
                    .ne(Notify::getUnreadId, ""); //添加 WHERE 条件，不等于
            notifyService.update(notifyUpdate);
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean agreeGroupApply(String userId, String fromId, String groupId) {
        //校验
        ChatGroup chatGroup = getById(groupId);
        if (chatGroup == null) {
            throw new BaseException("群聊不存在");
        }
        if (!userId.equals(chatGroup.getOwnerUserId())) {
            throw new BaseException("您不是群主，无权处理入群申请");
        }
        if (chatGroup.getStatus().equals(GroupStatus.Disable)) {
            throw new BaseException("该群已解散");
        }

        //查询该用户向该群发起的、状态为待处理的入群申请
        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notify::getFromId, fromId)
                .eq(Notify::getToId, groupId)
                .eq(Notify::getType, NotifyType.Group_Apply)
                .eq(Notify::getStatus, FriendApplyStatus.Wait);
        List<Notify> notifyList = notifyService.list(queryWrapper);

        if (notifyList.isEmpty()) {
            throw new BaseException("没有入群申请");
        }

        //加入群聊（已是成员则内部跳过）
        joinGroup(groupId, fromId);

        //更新通知状态为同意，未读方设为申请人
        notifyList.forEach(notify -> {
            notify.setStatus(FriendApplyStatus.Agree);
            notify.setUnreadId(fromId);
            notifyService.updateById(notify);
        });

        //通过 WebSocket 通知申请人
        webSocketService.sendNotifyToUser(notifyList.get(0), fromId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean rejectGroupApply(String userId, String fromId, String groupId) {
        //校验
        ChatGroup chatGroup = getById(groupId);
        if (chatGroup == null) {
            throw new BaseException("群聊不存在");
        }
        if (!userId.equals(chatGroup.getOwnerUserId())) {
            throw new BaseException("您不是群主，无权处理入群申请");
        }
        if (chatGroup.getStatus().equals(GroupStatus.Disable)) {
            throw new BaseException("该群已解散");
        }

        //查询该用户向该群发起的、状态为待处理的入群申请
        LambdaQueryWrapper<Notify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notify::getFromId, fromId)
                .eq(Notify::getToId, groupId)
                .eq(Notify::getType, NotifyType.Group_Apply)
                .eq(Notify::getStatus, FriendApplyStatus.Wait);
        List<Notify> notifyList = notifyService.list(queryWrapper);

        if (notifyList.isEmpty()) {
            throw new BaseException("没有入群申请");
        }

        //更新通知状态为拒绝，未读方设为申请人
        notifyList.forEach(notify -> {
            notify.setStatus(FriendApplyStatus.Reject);
            notify.setUnreadId(fromId);
            notifyService.updateById(notify);
        });

        //通过 WebSocket 通知申请人
        webSocketService.sendNotifyToUser(notifyList.get(0), fromId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean joinGroup(String groupId, String fromId) {
        ChatGroup chatGroup = getById(groupId);
        if (chatGroup == null) {
            throw new BaseException("群聊不存在");
        }
        if (chatGroup.getStatus().equals(GroupStatus.Disable)) {
            throw new BaseException("该群已解散");
        }

        //如果用户已经在群里了，直接返回，不重复加入
        String memberKey = "member:" + groupId + ":" + fromId;
        Boolean alreadyMember = (Boolean) redisUtils.get(memberKey);
        if (alreadyMember == null) {
            alreadyMember = chatGroupMemberService.isMemberExists(groupId, fromId);
            redisUtils.set(memberKey, alreadyMember, 30 * 60);
        }
        if (alreadyMember) {
            return false;
        }

        //加入群聊
        ChatGroupMember member = new ChatGroupMember();
        member.setId(IdUtil.randomUUID());
        member.setUserId(fromId);
        member.setChatGroupId(groupId);
        chatGroupMemberService.save(member);

        //群成员数 +1
        chatGroup.setMemberNum(chatGroup.getMemberNum() + 1);
        updateById(chatGroup);

        redisUtils.del(memberKey);

        //发送系统消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(groupId);
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.System);
        User newUser = userService.getById(fromId);
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(newUser.getName())
                .addContent("加入了该群");
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(fromId);
        msgContent.setExt(fromId);
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(fromId, UserRole.User, sendMsgVo, MsgType.System);

        return true;
    }
}
