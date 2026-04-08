package com.example.chatserver.service;

import com.example.chatserver.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
//todo 学习mq
public class MQProducerService {

    @Value("${rocketmq.producer.send-message-timeout}")
    private Integer messageTimeOut;

    @Value("${rocketmq.enabled}")
    private boolean enabled;

    private static final String topic = "chat";

    @Resource
    private RocketMQTemplate rocketMQTemplate;//RocketMQ 操作模板

/*
    方式	    方法	                    是否等待结果	是否阻塞	适用场景
    同步发送	sendMsg()	            ✅ 是	    ✅ 是	重要消息，需要确认
    异步发送	sendAsyncData()	        ✅ 是（回调）	❌ 否	重要消息，但不想阻塞
    单向发送	sendOneWayData()	    ❌ 否	    ❌ 否	日志、监控等不重要的消息
    延时发送	sendDelayData()     	✅ 是	    ✅ 是	定时任务、延迟处理*/

    /**
     * 普通发送
     */
    public void send(Object obj) {
        rocketMQTemplate.send(topic, MessageBuilder.withPayload(obj).build());
    }

    /**
     * 发送同步消息
     */
    public SendResult sendMsg(Message msgBody) {
        if (!enabled)
            return null;
        return rocketMQTemplate
                .syncSend(topic + ":msg", MessageBuilder.withPayload(msgBody).build());
    }

    /**
     * 发送异步消息
     */
    public void sendAsyncData(String msgBody) {
        rocketMQTemplate
                .asyncSend(topic, MessageBuilder.withPayload(msgBody).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // 处理消息发送成功逻辑
            }

            @Override
            public void onException(Throwable throwable) {
                // 处理消息发送异常逻辑
            }
        });
    }

    /**
     * 发送延时消息
     */
    public void sendDelayData(String msgBody, int delayLevel) {
        rocketMQTemplate
                .syncSend(topic, MessageBuilder.withPayload(msgBody).build(), messageTimeOut, delayLevel);
    }

    /**
     * 发送单向消息
     */
    public void sendOneWayData(String msgBody) {
        rocketMQTemplate
                .sendOneWay(topic, MessageBuilder.withPayload(msgBody).build());
    }

}
