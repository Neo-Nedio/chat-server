package com.example.chatserver.dto.voip;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LiveDanmakuDto {
    private String sessionId;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime createTime;
}
