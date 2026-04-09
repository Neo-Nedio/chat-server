package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.mapper.ChatGroupMapper;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.vo.chatGroup.CreateChatGroupVo;
import com.example.chatserver.vo.chatGroup.DetailsChatGroupVo;
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
    ChatGroupMapper chatGroupMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo) {
        //创建群聊
        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setId(IdUtil.randomUUID());
        chatGroup.setName(createChatGroupVo.getName());
        chatGroup.setNotice(createChatGroupVo.getNotice());
        chatGroup.setMemberNum(Optional.ofNullable(createChatGroupVo.getUsers()).map(ArrayList::size).orElse(0));
        chatGroup.setUserId(userId);
        chatGroup.setOwnerUserId(userId);
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
}
