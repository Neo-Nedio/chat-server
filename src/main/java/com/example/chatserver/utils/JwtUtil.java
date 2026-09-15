package com.example.chatserver.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil implements Serializable {
    @Serial
    private static final long serialVersionUID = -5625635588908941275L;

    // 令牌秘钥
    private static String secret;

    // 令牌有效期
    private static final int days = 30;

    @Value("${security.jwt-secret}")
    void setSecret(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("security.jwt-secret must not be empty");
        }
        secret = value;
    }

    /**
     * 获取token
     *
     */
    public static String createToken(Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expireTime = now.plus(days, ChronoUnit.DAYS);
        return Jwts.builder()
                .setIssuer("Neo")  // 设置签发人
                .setId(UUID.randomUUID().toString()) // 每次登录生成唯一令牌，避免快速重登时令牌相同
                .addClaims(claims)  // 添加自定义数据
                .setExpiration(Date.from(expireTime))              // 设置过期时间
                .signWith(SignatureAlgorithm.HS256, secret)        // 使用 HS256 算法签名
                .compact();                                        // 生成最终 Token 字符串
    }


    /**
     * 解析token
     *
     */
    public static Claims parseToken(String token) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(secret);  // 创建解析器（设置密钥）
        Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);    // 解析 Token（自动验证签名和过期）
        return claimsJws.getBody();                                  // 获取 Payload 中的 Claims
    }

}
