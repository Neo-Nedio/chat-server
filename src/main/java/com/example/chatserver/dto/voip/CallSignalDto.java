package com.example.chatserver.dto.voip;

import lombok.Data;

import java.util.List;

@Data
public class CallSignalDto {
    private String action;
    private String sessionId;
    private String fromUserId;
    private String groupId;
    private List<String> toUserIds;
    private String callType;
    private String sceneType;
}
