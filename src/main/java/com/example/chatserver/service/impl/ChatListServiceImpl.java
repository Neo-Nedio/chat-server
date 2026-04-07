package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ChatListMapper;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.vo.chatlist.CreateChatListVo;
import com.example.chatserver.vo.chatlist.DeleteChatListVo;
import com.example.chatserver.vo.chatlist.TopChatListVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;



@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

    @Resource
    ChatListMapper chatListMapper;

    @Resource
    FriendService friendService;


    //获取聊天记录
    @Override
    public ChatListDto getChatList(String userId) {
        ChatListDto chatListDto = new ChatListDto();
        //置顶
        chatListDto.setTops(chatListMapper.getChatListByUserIdAndIsTop(userId, true));
        //其他
        chatListDto.setOthers(chatListMapper.getChatListByUserIdAndIsTop(userId, false));
        return chatListDto;
    }

    //更新聊天记录
    @Override
    public void updateChatList(String toUserId, String fromUserId, MsgContent msgContent) {
        //判断好友的聊天列表是否存在
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, toUserId)    // 聊天记录的主人
                .eq(ChatList::getFromId, fromUserId); // 聊天对象（发送者）
        ChatList chatList = getOne(queryWrapper);
        if (null == chatList) {
            //新建会话
            chatList = new ChatList();
            chatList.setId(IdUtil.randomUUID());
            chatList.setIsTop(false);
            chatList.setUserId(toUserId);
            chatList.setFromId(fromUserId);
            chatList.setUnreadNum(1);
            chatList.setLastMsgContent(msgContent);
            save(chatList);
        } else {
            //更新会话
            chatList.setUnreadNum(chatList.getUnreadNum() + 1); //未读消息数加一
            chatList.setLastMsgContent(msgContent);
            updateById(chatList);
            //更新自己的聊天列表
            LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(ChatList::getLastMsgContent, JSONUtil.toJsonStr(msgContent))
                    .eq(ChatList::getUserId, fromUserId)
                    .eq(ChatList::getFromId, toUserId);
            update(new ChatList(), updateWrapper);
        }
    }

    //新建会话
    @Override
    public ChatList createChatList(String userId, CreateChatListVo createChatListVo) {
        boolean isFriend = friendService.isFriend(userId, createChatListVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        //查询是否有会话,没有则新建
        ChatList chatList = chatListMapper.detailChatList(userId, createChatListVo.getUserId());
        if (null != chatList)
            return chatList;
        //新建
        chatList = new ChatList();
        chatList.setId(IdUtil.randomUUID());
        chatList.setUserId(userId);
        chatList.setFromId(createChatListVo.getUserId());
        chatList.setUnreadNum(0);
        save(chatList);
        return chatList;
    }

    @Override
    public boolean messageRead(String userId, String targetId) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getUnreadNum, 0).
                eq(ChatList::getUserId, userId).
                eq(ChatList::getFromId, targetId);
        return update(updateWrapper);
    }

    @Override
    public ChatList detailChatList(String userId, String targetId) {
        return chatListMapper.detailChatList(userId, targetId);
    }

    @Override
    public boolean deleteChatList(String userId, DeleteChatListVo deleteChatListVo) {
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getId, deleteChatListVo.getChatListId())
                .eq(ChatList::getUserId, userId);
        return remove(queryWrapper);
    }

    @Override
    public boolean topChatList(String userId, TopChatListVo topChatListVo) {
        LambdaUpdateWrapper<ChatList> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ChatList::getIsTop, topChatListVo.isTop())
                .eq(ChatList::getId, topChatListVo.getChatListId())
                .eq(ChatList::getUserId, userId);
        return update(new ChatList(), updateWrapper);
    }

    @Override
    public int unread(String userId) {
        Integer num = chatListMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

    @Override
    public ChatList getChatListByUserIdAndFromId(String userId, String fromId) {
        LambdaQueryWrapper<ChatList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatList::getUserId, userId)
                .eq(ChatList::getFromId, fromId);
        return getOne(queryWrapper);
    }
}
