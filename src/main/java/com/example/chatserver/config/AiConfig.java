package com.example.chatserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    /**
     * 短期记忆条数（取会话最近 N 条消息作为上下文）
     */
    private Integer shortMemorySize = 20;
}