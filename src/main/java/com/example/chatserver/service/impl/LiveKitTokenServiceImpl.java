package com.example.chatserver.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.config.LiveKitConfig;
import com.example.chatserver.dto.voip.LiveKitRoomUserDto;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.LiveKitTokenService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LiveKitTokenServiceImpl implements LiveKitTokenService {
    @Resource
    LiveKitConfig liveKitConfig;

    @Resource
    UserService userService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    public String getHost() {
        liveKitConfig.validate();
        return liveKitConfig.getHost();
    }

    @Override
    public String createToken(String userId, String sessionId) {
        // 校验 LiveKit 配置是否完整（apiKey、apiSecret 等）
        liveKitConfig.validate();

        // 当前时间，作为签发时间
        Instant now = Instant.now();

        // 组装视频权限声明：允许加入房间、房间名、允许推流、允许订阅
        Map<String, Object> video = new HashMap<>();
        video.put("roomJoin", true);      // 允许加入房间
        video.put("room", sessionId);     // 房间名，用会话 ID
        video.put("canPublish", true);    // 允许推流（发自己的音视频）
        video.put("canSubscribe", true);  // 允许订阅（拉别人的音视频）

        // 把 video 权限塞进 claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("video", video);

        // 用 LiveKit 的 apiKey / apiSecret 签发 JWT token
        return Jwts.builder()
                .setIssuer(liveKitConfig.getApiKey())                                    // 签发者 = apiKey
                .setSubject(userId)                                                      // 主体 = 用户 ID
                .setIssuedAt(Date.from(now))                                             // 签发时间
                .setExpiration(Date.from(now.plusSeconds(liveKitConfig.getTokenTtlMinutes() * 60L))) // 过期时间 = 现在 + TTL 分钟
                .addClaims(claims)                                                       // 加入 video 权限声明
                .signWith(SignatureAlgorithm.HS256, liveKitConfig.getApiSecret())        // 用 apiSecret 做 HS256 签名
                .compact();                                                              // 生成最终 token 字符串
    }

    /**
     * 查询某会话（房间）里当前在线的参与者列表
     * 带重试：第一次失败会再试一次，两次都失败才报错
     */
    @Override
    public List<LiveKitRoomUserDto> listParticipants(String sessionId) {
        // 校验 LiveKit 配置是否完整
        liveKitConfig.validate();

        try {
            // 第一次尝试
            return listParticipantsOnce(sessionId);
        } catch (Exception first) {
            try {
                // 失败后重试一次
                return listParticipantsOnce(sessionId);
            } catch (Exception second) {
                // 两次都失败，抛业务异常
                throw new BaseException("LiveKit 服务不可用");
            }
        }
    }

    /**
     * 真正查询一次参与者列表
     * 流程：签临时 admin token → 调 LiveKit 的 ListParticipants 接口 → 解析返回并补全用户资料
     */
    private List<LiveKitRoomUserDto> listParticipantsOnce(String sessionId) throws Exception {
        // 构造 admin 权限声明：允许管理房间
        Map<String, Object> video = new HashMap<>();
        video.put("roomAdmin", true);

        // 签一个临时 token，5 分钟有效，用于调用 LiveKit 服务端接口
        String token = Jwts.builder()
                .setIssuer(liveKitConfig.getApiKey())
                .setExpiration(Date.from(Instant.now().plusSeconds(300)))   // 5 分钟过期
                .claim("video", video)                                       // admin 权限
                .signWith(SignatureAlgorithm.HS256, liveKitConfig.getApiSecret())
                .compact();

        // 拼接 LiveKit 的 HTTP 接口地址：把 ws/wss 换成 http/https
        String endpoint = liveKitConfig.getHost().replaceFirst("^ws", "http")
                + "/twirp/livekit.RoomService/ListParticipants";

        // 请求体：{"room": sessionId}
        String body = JSONUtil.createObj().set("room", sessionId).toString();

        // 构造 HTTP 请求：10 秒超时、Bearer token、JSON 格式、POST
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // 非 2xx 视为失败
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("LiveKit request failed");
        }

        // 解析返回里的 participants 数组
        JSONArray participants = JSONUtil.parseObj(response.body()).getJSONArray("participants");
        if (participants == null) return List.of();

        // 把每个 participant 转成 LiveKitRoomUserDto，并补全用户昵称和头像
        return participants.stream().map(item -> {
            JSONObject participant = (JSONObject) item;
            LiveKitRoomUserDto user = new LiveKitRoomUserDto();
            user.setUserId(participant.getStr("identity"));     // LiveKit 里用 identity 标识用户

            // 查本地用户资料，补全昵称和头像
            User profile = userService.getById(user.getUserId());
            if (profile != null) {
                user.setUsername(profile.getName());
                user.setAvatar(profile.getPortrait());
            } else {
                // 本地查不到就用 LiveKit 返回的 name
                user.setUsername(participant.getStr("name"));
            }

            user.setState(participant.getStr("state"));         // 参与者状态
            return user;
        }).collect(Collectors.toList());
    }
}
