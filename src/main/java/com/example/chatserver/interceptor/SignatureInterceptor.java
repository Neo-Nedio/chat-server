package com.example.chatserver.interceptor;


import com.example.chatserver.entity.Conversation;
import com.example.chatserver.service.ConversationService;
import com.example.chatserver.utils.SignatureUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/*

        ┌─────────────────────────────────────────────────────────────────────────────────────┐
        │                              API 签名验证完整流程                                      │
        └─────────────────────────────────────────────────────────────────────────────────────┘

   ┌─────────────────────────┐                              ┌─────────────────────────┐
   │        客户端            │                              │        服务端            │
   │   （调用方 / 第三方）      │                              │    （你的 API 服务）      │
   ├─────────────────────────┤                              ├─────────────────────────┤
   │                         │                              │                         │
   │  accessKey: "ak_123"    │                              │  存储的密钥映射：         │
   │  secretKey: "sk_abc"    │                              │  ┌───────────────────┐   │
   │                         │                              │  │ ak_123 → sk_abc   │   │
   └───────────┬─────────────┘                              │  │ ak_456 → sk_def   │   │
               │                                            │  └───────────────────┘   │
               │                                            │                         │
               │  1. 构造待签名字符串                         │                         │
               │     ┌─────────────────────────────────┐    │                         │
               │     │ stringToSign =                   │    │                         │
               │     │ method + path + accessKey + ts   │    │                         │
               │     │                                   │    │                         │
               │     │ 例："GET/api/userak_1231700000000"│    │                         │
               │     └─────────────────────────────────┘    │                         │
               │                    │                       │                         │
               │                    ▼                       │                         │
               │  2. 用 secretKey 加密                       │                         │
               │     ┌─────────────────────────────────┐    │                         │
               │     │ signature = HMAC-SHA256(         │    │                         │
               │     │   secretKey, stringToSign        │    │                         │
               │     │ )                               │    │                         │
               │     │                                   │    │                         │
               │     │ 结果："a1b2c3d4e5f6..."           │    │                         │
               │     └─────────────────────────────────┘    │                         │
               │                    │                       │                         │
               │                    ▼                       │                         │
               │  3. 发送 HTTP 请求                         │                         │
               │     ┌─────────────────────────────────┐    │                         │
               │     │ POST /api/user/login             │    │                         │
               │     │ Headers:                         │    │                         │
               │     │   X-Access-Key: ak_123           │    │                         │
               │     │   X-Timestamp: 1700000000        │    │                         │
               │     │   X-Signature: a1b2c3d4...       │    │                         │
               │     └─────────────────────────────────┘    │                         │
               │                    │                       │                         │
               │                    └──────────────────────>│                         │
               │                                            │                         │
               │                                            │  4. 提取请求参数         │
               │                                            │     ┌───────────────┐    │
               │                                            │     │ method = POST │    │
               │                                            │     │ path = /api/..│    │
               │                                            │     │ accessKey     │    │
               │                                            │     │ timestamp     │    │
               │                                            │     │ clientSig     │    │
               │                                            │     └───────────────┘    │
               │                                            │                    │     │
               │                                            │                    ▼     │
               │                                            │  5. 根据 accessKey       │
               │                                            │     查找 secretKey        │
               │                                            │     ┌───────────────┐    │
               │                                            │     │ ak_123        │    │
               │                                            │     │   ↓           │    │
               │                                            │     │ sk_abc        │    │
               │                                            │     └───────────────┘    │
               │                                            │                    │     │
               │                                            │                    ▼     │
               │                                            │  6. 服务端重新计算签名     │
               │                                            │     ┌───────────────┐    │
               │                                            │     │ serverSig =   │    │
               │                                            │     │ HMAC-SHA256(  │    │
               │                                            │     │ sk_abc,       │    │
               │                                            │     │ stringToSign  │    │
               │                                            │     │ )             │    │
               │                                            │     └───────────────┘    │
               │                                            │                    │     │
               │                                            │                    ▼     │
               │                                            │  7. 对比签名             │
               │                                            │     ┌─────────────┐      │
               │                                            │     │ clientSig ==│      │
               │                                            │     │ serverSig ?  │      │
               │                                            │     └─────────────┘      │
               │                                            │              │          │
               │                                            │      ┌───────┴───────┐  │
               │                                            │      │               │  │
               │                                            │      ▼               ▼  │
               │                                            │   ✅ 相同          ❌ 不同│
               │                                            │      │               │  │
               │                                            │      ▼               ▼  │
               │                                            │  返回数据       返回401   │
               │                                            │      │               │  │
               │  8. 收到响应                                 │      │               │  │
               │  <─────────────────────────────────────────┘      │               │  │
               │                                            │                         │
               └────────────────────────────────────────────┘                         │
                                                                                      │
└─────────────────────────────────────────────────────────────────────────────────────┘*/
@Component
//前端会生成一个签名，后端通过参数再次生成一个进行比较，比较成功则证明前端知道完整的公钥和密钥，验证成功
public class SignatureInterceptor implements HandlerInterceptor {
    ConversationService conversationService;

    public SignatureInterceptor(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, Object handler) throws Exception {
        String accessKey = request.getHeader("X-Access-Key");
        String timestamp = request.getHeader("X-Timestamp");
        String signature = request.getHeader("X-Signature");
        // 验证必要参数
        if (accessKey == null || timestamp == null || signature == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 验证时间戳，防止重放攻击
        long now = System.currentTimeMillis();
        long requestTime = Long.parseLong(timestamp);
        if (Math.abs(now - requestTime) > 300000) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 根据accessKey获取secretKey
        Conversation conversation = conversationService.getConversationByAccessKey(accessKey);
        if (null == conversation) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 验证签名
        String method = request.getMethod();
        String path = request.getRequestURI();
        String calculatedSignature = SignatureUtils.calculateSignature(method, path, accessKey, timestamp, conversation.getSecretKey());
        if (!calculatedSignature.equals(signature)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        //将信息放入请求头
        Map<String, Object> map = new HashMap<>();
        map.put("accessKey", accessKey);
        map.put("timestamp", timestamp);
        map.put("userId", conversation.getUserId());
        request.setAttribute("userinfo", map);
        return true;
    }
}
