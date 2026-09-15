package com.example.chatserver.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.constant.MsgType;
import com.example.chatserver.constant.UserStatus;
import com.example.chatserver.constant.WsContentType;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.User;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.JwtUtil;
import com.example.chatserver.utils.ResultUtil;
import io.jsonwebtoken.Claims;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//WebSocket连接使用的方法
@Service
public class WebSocketService {

    @Data
    public static class WsContent {
        private String type;
        private Object content;
    }

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Lazy
    @Resource
    UserService userService;

    //核心数据结构（在线用户存储）
    public static final ConcurrentHashMap<String, Channel> Online_User = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Channel, String> Online_Channel = new ConcurrentHashMap<>();

    public boolean isOnline(String userId) {
        Channel channel = Online_User.get(userId);
        return channel != null && channel.isActive();
    }

    //用户上线
    public void online(Channel channel, String token) {
        try {
            Claims claims = JwtUtil.parseToken(token);
            String userId = (String) claims.get("userId");

            // 先检查是否被禁用，禁用则通知后直接关闭，不加入在线列表
            User user = userService.getById(userId);
            if (user.getStatus().equals(UserStatus.Disable)) {
                sendMsg(channel, "您的账号已被管理员禁用", WsContentType.Disable);
                channel.close();
                return;
            }

            // 同一账号只保留最新连接；旧设备收到通知后被关闭。
            // 用put，将替换和删除同时进行，不会出错
            Channel previousChannel = Online_User.put(userId, channel);
            Online_Channel.put(channel, userId);
            if (previousChannel != null && previousChannel != channel) {
                sendMsg(previousChannel, "您的账号已在其他设备登录，当前设备已下线", WsContentType.Disable);
                previousChannel.close();
            }
            userService.online(userId);
        } catch (Exception e) {
            sendMsg(channel, ResultUtil.Fail("连接错误"), WsContentType.Msg);
            channel.close();
        }
    }

    //用户离线
    public void offline(Channel channel) {
        String userId = Online_Channel.remove(channel);  // 通过channel找userId
        if (StrUtil.isNotBlank(userId)) { //移除
            // 旧连接关闭时，不能清除已经替换进来的新连接，也不能把用户标记为离线。
            // 只有 Map 里存的确实是这条 channel 才删成功
            if (Online_User.remove(userId, channel)) {
                userService.offline(userId);
            }
        }
    }

    //发送消息（私有）
    private boolean sendMsg(Channel channel, Object msg, String type) {
        if (channel == null || !channel.isActive()) {
            return false;
        }
        //writeAndFlush	Netty方法  立即将数据写入网络缓冲区并刷出（发送给客户端）
        //TextWebSocketFrame	Netty的WebSocket文本帧类型  将普通字符串包装成 WebSocket 协议规定的文本帧格式
        WsContent wsContent = new WsContent();
        wsContent.setType(type);
        wsContent.setContent(msg);
        try {
            channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(wsContent)));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void logout(String userId, String token) {
        Channel channel = Online_User.get(userId);
        if (channel == null || token == null || !token.equals(NettyUtil.getAttr(channel, NettyUtil.TOKEN))) {
            return;
        }

        try {
            offline(channel);
        } finally {
            channel.close();
        }
    }

    //发送给指定用户，privateChat 用于区分私聊和群聊/通知类消息
    public boolean sendMsgToUser(Object msg, String userId, boolean privateChat) {
        Channel channel = Online_User.get(userId);
        return sendMsg(channel, msg, WsContentType.Msg);
    }

    //发送给群聊用户
    public List<String> sendMsgToGroup(Message message, String groupId) {
        List<String> failedUserIds = new ArrayList<>();
        List<ChatGroupMember> list = chatGroupMemberService.getGroupMember(groupId);
        for (ChatGroupMember member : list) {
            //将消息发送给群内的所有成员（发送者除外，除非是系统消息）
            if (!message.getFromId().equals(member.getUserId()) || MsgType.System.equals(message.getType())) {
                if (!sendMsgToUser(message, member.getUserId(), false)) {
                    failedUserIds.add(member.getUserId());
                }
            }
        }
        return failedUserIds;
    }

    // 发送给所有在线用户
    public void sendAll(Object msg) {
        //channel：遍历出来的每个连接的 Channel 对象
        //ext：与该 Channel 绑定的用户ID（扩展信息）
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, msg, WsContentType.Msg);
        });
    }

    //发送通知给某个用户
    public void sendNotifyToUser(Object msg, String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.Notify);
        }
    }

    public void sendNoticeToGroup(Message message, String groupId) {
        List<ChatGroupMember> list = chatGroupMemberService.getGroupMember(groupId);
        for (ChatGroupMember member : list) {
            if (!message.getFromId().equals(member.getUserId()) || MsgType.System.equals(message.getType())) {
                sendNotifyToUser(message, member.getUserId());
            }
        }
    }

    public void sendVideoToUser(Object msg, String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, msg, WsContentType.Video);
        }
    }

    public void sendCallToUser(Object message, String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null && channel.isActive()) {
            sendMsg(channel, message, WsContentType.Call);
        }
    }

    //发送通知给全体用户
    public void sendNotifyAll(Object msg) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, msg, WsContentType.Notify);
        });
    }

    //发送系统通知给全体用户
    public void sendSystemNotifyAll(Object notify) {
        Online_Channel.forEach((channel, ext) -> {
            sendMsg(channel, notify, WsContentType.SystemNotify);
        });
    }

    //禁用用户
    public void sendDisableToUser(String userId) {
        Channel channel = Online_User.get(userId);
        if (channel != null) {
            sendMsg(channel, "您的账号已被管理员禁用", WsContentType.Disable);
            offline(channel);
            channel.close();
        }
    }

    public Integer getOnlineNum() {
        return Online_User.size();
    }
}
