package com.example.chatserver.websocket;

import cn.hutool.extra.spring.SpringUtil;
import com.example.chatserver.service.WebSocketService;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable // 标记为可共享（单例模式）
//Netty WebSocket 服务端处理器，负责管理 WebSocket 连接的生命周期和业务处理。
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> { //处理 WebSocket 文本帧（自动释放资源）

    private WebSocketService webSocketService;

    //初始化依赖
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (this.webSocketService == null) {
            this.webSocketService = SpringUtil.getBean(WebSocketService.class);
        }
    }

    //离线处理
    private void offLine(ChannelHandlerContext ctx) {
        webSocketService.offline(ctx.channel());
        ctx.channel().close(); //关闭网络连接
    }

    /**
     * Handler 被移除
     作用：Handler 从 Pipeline 中移除时，执行离线清理。
     触发时机：
     Channel 关闭
     Pipeline 动态移除 Handler
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        offLine(ctx);
    }

    /**
     *  Channel 失效
     作用：Channel 变成非活跃状态时，执行离线清理。
     触发时机：
     网络连接断开
     客户端关闭浏览器
     网络异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        offLine(ctx);
    }

    /**
     * 心跳检查
     * 用户事件触发（核心）
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception { //evt	触发的事件对象
        // 1. 心跳超时处理
        if (evt instanceof IdleStateEvent idleStateEvent) {
            if (idleStateEvent.state() == IdleState.READER_IDLE) { //READER_IDLE	读空闲（没收到数据）	30秒
                offLine(ctx);  // 30秒没收到消息，断开连接
            }
        }
        // 2. WebSocket 握手完成处理
        else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            // 从 Channel 属性中获取 token（之前由 HttpHeadersHandler 存储）
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            //调用 webSocketService.online() 将用户标记为在线
            webSocketService.online(ctx.channel(), token);
        }
        //事件传播 将事件继续传递给 Pipeline 中的下一个 Handler。
        super.userEventTriggered(ctx, evt);
    }

    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常关闭
        ctx.channel().close();
    }

    //消息接收
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println(msg.text());
        //只发送消息,不接受消息
    }
}
