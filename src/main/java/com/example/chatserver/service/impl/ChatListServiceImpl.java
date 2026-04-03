package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.mapper.ChatListMapper;
import com.example.chatserver.service.ChatListService;
import org.springframework.stereotype.Service;


@Service
public class ChatListServiceImpl extends ServiceImpl<ChatListMapper, ChatList> implements ChatListService {

}
