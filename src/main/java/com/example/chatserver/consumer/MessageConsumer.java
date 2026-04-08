package com.example.chatserver.consumer;


import com.example.chatserver.entity.Message;
import com.example.chatserver.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
@RocketMQMessageListener(topic = "chat", selectorExpression = "msg", consumerGroup = "chat_group")
public class MessageConsumer implements RocketMQListener<Message> {

    @Resource
    WebSocketService webSocketService;

    @Override
    public void onMessage(Message msg) {
        //发送消息
        webSocketService.sendMsgToUser(msg, msg.getToId());
    }
}
