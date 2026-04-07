package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.MessageRetraction;
import com.example.chatserver.mapper.MessageRetractionMapper;
import com.example.chatserver.service.MessageRetractionService;
import org.springframework.stereotype.Service;


@Service
public class MessageRetractionServiceImpl extends ServiceImpl<MessageRetractionMapper, MessageRetraction> implements MessageRetractionService {

}
