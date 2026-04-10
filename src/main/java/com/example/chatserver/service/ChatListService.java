package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.vo.chatlist.CreateChatListVo;
import com.example.chatserver.vo.chatlist.DeleteChatListVo;
import com.example.chatserver.vo.chatlist.DetailChatListVo;
import com.example.chatserver.vo.chatlist.TopChatListVo;


public interface ChatListService extends IService<ChatList> {

    ChatListDto getChatList(String userId);

    void updateChatList(String toUserId, String fromUserId, MsgContent msgContent, String type);

    ChatList createChatList(String userId, String role, CreateChatListVo createChatListVo);

    boolean messageRead(String userId, String targetId);

    ChatList detailChatList(String userId, DetailChatListVo detailChatListVo);

    boolean deleteChatList(String userId, DeleteChatListVo deleteChatListVo);

    boolean topChatList(String userId, TopChatListVo topChatListVo);

    int unread(String userId);

    ChatList getChatListByUserIdAndFromId(String userId, String fromId);

    boolean messageReadAll(String userId);

    void updateChatListGroup(String groupId, MsgContent msgContent);

    void removeByUserId(String userId, String friendId);
}
