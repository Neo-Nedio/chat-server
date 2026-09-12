package com.example.chatserver.dto.voip;

import lombok.Data;

@Data
public class LiveResultDto {
    private String sessionId;
    private String token;
    private String sceneType;
}
