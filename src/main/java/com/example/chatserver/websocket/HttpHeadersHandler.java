package com.example.chatserver.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.ObjectUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

//这是一个 Netty 的 ChannelHandler，用于从 HTTP 请求中提取 Token 和 IP 地址，并将其存储到 Channel 的属性中。
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 是完整的 HTTP 请求
        if (msg instanceof FullHttpRequest request) { //FullHttpRequest：Netty 中表示完整 HTTP 请求的对象
            //提取 Token
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.uri());
            String token = Optional.ofNullable(urlBuilder.getQuery()) //获取查询参数 Map
                    .map(k -> k.get("x-token")) //获取名为 x-token 的参数
                    .map(CharSequence::toString)
                    .orElse("");

            //存储 Token 到 Channel
            NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, token);

            //清理 URI（去掉参数）
            request.setUri(urlBuilder.getPath().toString());

            // 提取 IP 地址
            HttpHeaders headers = request.headers();
            String ip = headers.get("x-ip"); //优先从请求头 x-ip 获取（通常由 Nginx 等代理服务器添加）
            //如果没有，则从 Channel 的远程地址获取（客户端真实 IP）
            if (ObjectUtils.isEmpty(ip)) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            //存储 ip 到 Channel
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);


            ctx.pipeline().remove(this);   // 移除当前 Handler
            ctx.fireChannelRead(request);   // 将请求传递给下一个 Handler
        } else {
            ctx.fireChannelRead(msg); // 不是就继续传递
        }
    }
}