package com.example.chatserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "livekit")
public class LiveKitConfig {
    private String host;
    private String apiKey;
    private String apiSecret;
    private int tokenTtlMinutes = 60;

    public void validate() {
        if (host == null || host.isBlank() || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("LiveKit 配置不完整");
        }
        if (tokenTtlMinutes < 5 || tokenTtlMinutes > 60) {
            throw new IllegalStateException("LiveKit token 有效期必须在 5 到 60 分钟之间");
        }
    }
}
