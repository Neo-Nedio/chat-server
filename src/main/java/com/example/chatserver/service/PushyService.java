package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pushy 离线推送服务。
 *
 * <p>Pushy 的 Secret API Key 只能在服务端使用，不能下发到客户端。</p>
 */
@Service
@Slf4j
public class PushyService {

    private static final String PUSH_URL = "https://api.pushy.me/push";
    private static final int MAX_TOKEN_COUNT = 100_000;
    private static final int MAX_DATA_LENGTH = 4 * 1024;

    @Resource
    private RestTemplate restTemplate;

    @Value("${pushy.api-key:}")
    private String apiKey;

    /** Pushy 未指定时默认保留 30 天，最长不超过 365 天。 */
    @Value("${pushy.time-to-live:2592000}")
    private int timeToLive;

    /**
     * 向一批设备发送同一份 JSON 消息。
     *
     * @param pushyTokens Pushy 设备 Token
     * @param msg         直接作为 Pushy 请求体中的 data 字段发送的对象
     * @return Pushy 返回结果，包含推送 id、设备数以及无效 Token（如果有）
     */
    public JSONObject send(Collection<String> pushyTokens, Object msg) {
        if (pushyTokens == null || pushyTokens.isEmpty()) {
            throw new BaseException("Pushy设备Token不能为空");
        }
        if (msg == null) {
            throw new BaseException("Pushy推送消息不能为空");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BaseException("Pushy API Key未配置");
        }
        if (timeToLive < 0 || timeToLive > 365 * 24 * 60 * 60) {
            throw new BaseException("Pushy离线保留时间必须在0到365天之间");
        }

        List<String> tokens = pushyTokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (tokens.isEmpty()) {
            throw new BaseException("Pushy设备Token不能为空");
        }
        if (tokens.size() > MAX_TOKEN_COUNT) {
            throw new BaseException("Pushy单次最多发送给100000个设备");
        }

        String messageJson = cn.hutool.json.JSONUtil.toJsonStr(msg);
        if (messageJson.getBytes(StandardCharsets.UTF_8).length > MAX_DATA_LENGTH) {
            throw new BaseException("Pushy消息不能超过4KB");
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("to", tokens);
        requestBody.put("data", msg);
        requestBody.put("time_to_live", timeToLive);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        String url = UriComponentsBuilder.fromUriString(PUSH_URL)
                .queryParam("api_key", apiKey)
                .toUriString();

        try {
            ResponseEntity<JSONObject> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    JSONObject.class
            );
            JSONObject responseBody = response.getBody();
            if (responseBody == null) {
                throw new BaseException("Pushy响应为空");
            }
            return responseBody;
        } catch (HttpStatusCodeException e) {
            log.error("Pushy推送失败，HTTP状态码={}，响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BaseException("Pushy推送失败: " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            log.error("Pushy服务连接失败", e);
            throw new BaseException("Pushy服务连接失败");
        }
    }

    /**
     * 从用户列表中提取 Pushy Token，并一次性发送同一份消息。
     */
    public JSONObject sendToUsers(Collection<User> users, Object msg) {
        if (users == null || users.isEmpty()) {
            return null;
        }
        List<String> pushyTokens = users.stream()
                .filter(user -> user != null && user.getPushyToken() != null
                        && !user.getPushyToken().isBlank())
                .map(User::getPushyToken)
                .map(String::trim)
                .distinct()
                .toList();
        if (pushyTokens.isEmpty()) {
            return null;
        }
        return send(pushyTokens, msg);
    }
}
