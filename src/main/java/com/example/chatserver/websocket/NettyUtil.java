package com.example.chatserver.websocket;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

// Netty 的工具类，用于方便地在 Channel 上存储和获取自定义属性。
public class NettyUtil {

    //AttributeKey<String>	Netty 的属性键，泛型指定值类型为 String
    public static AttributeKey<String> IP = AttributeKey.valueOf("x-ip");
    public static AttributeKey<String> TOKEN = AttributeKey.valueOf("x-token");

    // 设置属性
    public static <T> void setAttr(Channel channel, AttributeKey<T> attributeKey, T data) {
        Attribute<T> attr = channel.attr(attributeKey);
        attr.set(data);
    }

    //获取属性
    public static <T> T getAttr(Channel channel, AttributeKey<T> attributeKey) {
        return channel.attr(attributeKey).get();
    }
}
