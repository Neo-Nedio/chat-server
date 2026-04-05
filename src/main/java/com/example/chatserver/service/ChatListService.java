package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.ext.MsgContent;


public interface ChatListService extends IService<ChatList> {

    ChatListDto getChatList(String userId);

    void updateChatList(String toUserId, String fromUserId, MsgContent msgContent);
}
