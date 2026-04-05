package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.vo.chatlist.CreateChatListVo;


public interface ChatListService extends IService<ChatList> {

    ChatListDto getChatList(String userId);

    void updateChatList(String toUserId, String fromUserId, MsgContent msgContent);

    ChatList createChatList(String userId, CreateChatListVo createChatListVo);

    boolean messageRead(String userId, String targetId);

    ChatList detailChatList(String userId, String targetId);
}
