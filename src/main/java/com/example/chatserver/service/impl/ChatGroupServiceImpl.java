package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.config.MinioConfig;
import com.example.chatserver.constant.MessageContentType;
import com.example.chatserver.constant.MsgSource;
import com.example.chatserver.constant.MsgType;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.User;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ChatGroupMapper;
import com.example.chatserver.service.*;
import com.example.chatserver.vo.chatGroup.*;
import com.example.chatserver.vo.message.SendMsgVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ChatGroupServiceImpl extends ServiceImpl<ChatGroupMapper, ChatGroup> implements ChatGroupService {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    ChatListService chatListService;

    @Resource
    MessageService messageService;

    @Resource
    UserService userService;

    @Resource
    ChatGroupMapper chatGroupMapper;

    @Resource
    MinioConfig minioConfig;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo) {
        //创建群聊
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(IdUtil.randomUUID());
        chatGroup.setName(createChatGroupVo.getName());
        chatGroup.setMemberNum(Optional.ofNullable(createChatGroupVo.getUsers()).map(ArrayList::size).orElse(0) + 1 );
        chatGroup.setUserId(userId);
        chatGroup.setOwnerUserId(userId);
        chatGroup.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-group-portrait.png");
        boolean isSava = save(chatGroup);
        //添加自己
        ChatGroupMember chatGroupMember = new ChatGroupMember();
        chatGroupMember.setId(IdUtil.randomUUID());
        chatGroupMember.setChatGroupId(chatGroup.getId());
        chatGroupMember.setUserId(userId);
        chatGroupMemberService.save(chatGroupMember);
        //绑定群成员
        if (isSava && null != createChatGroupVo.getUsers()) {
            for (CreateChatGroupVo.User user : createChatGroupVo.getUsers()) {
                chatGroupMember = new ChatGroupMember();
                chatGroupMember.setId(IdUtil.randomUUID());
                chatGroupMember.setChatGroupId(chatGroup.getId());
                chatGroupMember.setUserId(user.getUserId());
                chatGroupMember.setGroupRemark(user.getName());
                chatGroupMemberService.save(chatGroupMember);
            }
        }
        return isSava;
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
    public boolean updateGroupPortrait(String groupId, String url) {
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getPortrait, url)
                .eq(ChatGroup::getId, groupId);
        return update(updateWrapper);
    }

    @Override
    public boolean updateChatGroupName(String userId, UpdateChatGroupNameVo updateChatGroupNameVo) {
        if (!isOwner(updateChatGroupNameVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        LambdaUpdateWrapper<ChatGroup> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatGroup::getName, updateChatGroupNameVo.getName())
                .eq(ChatGroup::getId, updateChatGroupNameVo.getGroupId());
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
        for (String userid : inviteMemberVo.getUserIds()) {
            ChatGroupMember member = new ChatGroupMember();
            member.setId(IdUtil.randomUUID());
            member.setUserId(userid);
            member.setChatGroupId(inviteMemberVo.getGroupId());
            members.add(member);
        }
        if (!members.isEmpty()) {
            ChatGroup chatGroup = getById(inviteMemberVo.getGroupId());
            chatGroup.setMemberNum(chatGroup.getMemberNum() + members.size());
            updateById(chatGroup);
            return chatGroupMemberService.saveBatch(members);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo) {
        //从群聊中移出
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, quitChatGroupVo.getGroupId());
        chatGroupMemberService.remove(queryWrapper);

        //删除会话
        LambdaQueryWrapper<ChatList> chatListLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatListLambdaQueryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getFromId, quitChatGroupVo.getGroupId());
        chatListService.remove(chatListLambdaQueryWrapper);

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

        //踢出群成员
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, kickChatGroupVo.getGroupId())
                .eq(ChatGroupMember::getUserId, kickChatGroupVo.getUserId());
        chatGroupMemberService.remove(queryWrapper);

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(kickChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(kickChatGroupVo.getUserId());
        msgContent.setContent(user.getName());
        msgContent.setFromUserId(userId);
        msgContent.setExt(kickChatGroupVo.getUserId());
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, sendMsgVo, MsgType.System);

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

        //踢出所有成员
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, dissolveChatGroupVo.getGroupId());
        chatGroupMemberService.remove(queryWrapper);

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(dissolveChatGroupVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        msgContent.setFromUserId(userId);
        msgContent.setExt("all"); //全部人被踢出，就是解散群聊
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, sendMsgVo, MsgType.System);

        //解散群聊
        return removeById(dissolveChatGroupVo.getGroupId());
    }

    @Override
    public boolean transferChatGroup(String userId, TransferChatGroupVo transferChatGroupVo) {
        if (!isOwner(transferChatGroupVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        //更换群主
        ChatGroup chatGroup = getById(transferChatGroupVo.getGroupId());
        chatGroup.setOwnerUserId(transferChatGroupVo.getUserId());
        return updateById(chatGroup);
    }
}
