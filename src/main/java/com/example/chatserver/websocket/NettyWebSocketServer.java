package com.example.chatserver.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
//基于 Netty 的 WebSocket 服务器配置类，使用 Spring Boot 的 @Configuration 管理生命周期。
public class NettyWebSocketServer {

    //端口号
    public static final int Web_Socket_Port = 9100;
    //自定义业务处理器（单例）
    public static final NettyWebSocketServerHandler Netty_Web_Socket_Server_Handler = new NettyWebSocketServerHandler();
    //bossGroup（接收连接线程组） 负责接受客户端的连接请求，然后将连接注册到 workerGroup
    /*    1	线程数 = 1，只需要一个线程接收连接
       */
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    //workerGroup（处理连接线程组）
/*          NettyRuntime.availableProcessors()	获取 CPU 核心数（如 8 核就是 8）
            */
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    /**
     * 启动 ws server
     */
    @PostConstruct //在 Bean 初始化完成后自动执行特定方法
    public void start() throws InterruptedException {
        run();
    }

    /**
     * 销毁
     */
    @PreDestroy //Spring 容器销毁 Bean 之前
    public void destroy() {
        //优雅关闭线程组（不接新连接 等待已有任务执行完成）
        Future<?> future = bossGroup.shutdownGracefully();
        Future<?> future1 = workerGroup.shutdownGracefully();
        //同步等待关闭完成
        future.syncUninterruptibly();
        future1.syncUninterruptibly();
        log.info("销毁成功");
    }

    public void run() throws InterruptedException {
        // 服务器启动引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup) // 配置线程组
                .channel(NioServerSocketChannel.class) //指定使用 NIO 模式的服务器 Socket 通道
                .option(ChannelOption.SO_BACKLOG, 128) //已完成握手的连接队列最大长度
                .option(ChannelOption.SO_KEEPALIVE, true) //开启 TCP 心跳保活
                .handler(new LoggingHandler(LogLevel.INFO)) // 为 bossGroup 添加 日志处理器
                .childHandler(new ChannelInitializer<SocketChannel>() { //为每个客户端连接初始化 Pipeline，添加处理器链。
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 入站 Handler（按顺序执行）
                        // ① 心跳检测
                        pipeline.addLast(new IdleStateHandler(30, 0, 0));
                        // ② HTTP 解码
                        pipeline.addLast(new HttpServerCodec());
                        // ③ 分块写入
                        pipeline.addLast(new ChunkedWriteHandler());
                        // ④ 消息聚合
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        // ⑤ 提取 Token/IP
                        pipeline.addLast(new HttpHeadersHandler());
                        // ⑥ WebSocket 升级
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                        // ⑦ 业务处理
                        pipeline.addLast(Netty_Web_Socket_Server_Handler);
                    }
                });
        // Netty 服务器启动并绑定端口
        //sync() 确保服务器完全启动后才继续
        serverBootstrap.bind(Web_Socket_Port).sync();
    }

}
