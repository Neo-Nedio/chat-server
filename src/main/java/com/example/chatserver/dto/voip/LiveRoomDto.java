package com.example.chatserver.dto.voip;

import lombok.Data;

@Data
public class LiveRoomDto {
    private String sessionId;
    private String userId;
    private int participantCount;
}
