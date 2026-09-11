package com.example.chatserver.dto.voip;

import lombok.Data;

@Data
public class CallInviteDto {
    private String sessionId;
    private String groupId;
    private String callType;
    private String sceneType;
}
