package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.vo.chatGroup.*;

import java.util.List;


public interface ChatGroupService extends IService<ChatGroup> {

    boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo);

    List<ChatGroup> chatGroupList(String userId);

    ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupVo detailsChatGroupVo);

    boolean isOwner(String groupId, String userId);

    boolean updateGroupPortrait(String groupId, String fileName);

    boolean updateChatGroupName(String userId, UpdateChatGroupNameVo updateChatGroupNameVo);

    boolean updateChatGroup(String userId, UpdateChatGroupVo updateChatGroupVo);

    boolean inviteMember(String userId, InviteMemberVo inviteMemberVo);

    boolean quitChatGroup(String userId, QuitChatGroupVo quitChatGroupVo);

    boolean kickChatGroup(String userId, KickChatGroupVo kickChatGroupVo);

    boolean dissolveChatGroup(String userId, DissolveChatGroupVo dissolveChatGroupVo);

    boolean transferChatGroup(String userId, TransferChatGroupVo transferChatGroupVo);
}
