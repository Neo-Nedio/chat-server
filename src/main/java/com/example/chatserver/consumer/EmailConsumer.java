package com.example.chatserver.consumer;

import com.example.chatserver.dto.EmailTaskDto;
import com.example.chatserver.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@RocketMQMessageListener(topic = "chat", selectorExpression = "email", consumerGroup = "chat_email")
public class EmailConsumer implements RocketMQListener<EmailTaskDto> {

    @Resource
    EmailService emailService;

    @Override
    public void onMessage(EmailTaskDto task) {
        emailService.doSend(task);
    }
}
