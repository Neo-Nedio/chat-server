package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.mapper.ChatListMapper;
import com.example.chatserver.service.ChatListService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

    @Resource
    ChatListMapper chatListMapper;


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
            //新建
            chatList = new ChatList();
            chatList.setId(IdUtil.randomUUID());
            chatList.setIsTop(false);
            chatList.setUserId(toUserId);
            chatList.setFromId(fromUserId);
            chatList.setUnreadNum(1);
            chatList.setLastMsgContent(msgContent);
            save(chatList);
        } else {
            //更新
            chatList.setUnreadNum(chatList.getUnreadNum() + 1);
            chatList.setLastMsgContent(msgContent);
            updateById(chatList);
        }
    }
}
