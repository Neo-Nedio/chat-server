package com.example.chatserver.consumer;

import com.example.chatserver.constant.MsgSource;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.User;
import com.example.chatserver.service.PushyService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
@RocketMQMessageListener(topic = "chat", selectorExpression = "user", consumerGroup = "chat_user")
public class UserMessageConsumer implements RocketMQListener<Message> {

    @Resource
    WebSocketService webSocketService;

    @Resource
    UserService userService;

    @Resource
    PushyService pushyService;

    @Override
    public void onMessage(Message msg) {
        //发送消息
        if (!webSocketService.sendMsgToUser(msg, msg.getToId(), MsgSource.User.equals(msg.getSource()))
                && MsgSource.User.equals(msg.getSource())) {
            List<User> users = userService.getUsersByIds(List.of(msg.getToId()));
            pushyService.sendToUsers(users, msg);
        }
    }
}
