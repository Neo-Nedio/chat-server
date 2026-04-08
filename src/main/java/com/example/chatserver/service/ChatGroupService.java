package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.vo.chatGroup.CreateChatGroupVo;
import com.example.chatserver.vo.chatGroup.DetailsChatGroupVo;

import java.util.List;


public interface ChatGroupService extends IService<ChatGroup> {

    boolean createChatGroup(String userId, CreateChatGroupVo createChatGroupVo);

    List<ChatGroup> chatGroupList(String userId);

    ChatGroupDetailsDto detailsChatGroup(String userId, DetailsChatGroupVo detailsChatGroupVo);
}
