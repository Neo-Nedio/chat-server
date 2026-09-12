package com.example.chatserver.dto.voip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiveRoomDto {
    private String sessionId;
    private String userId;
    private String title;
    private String background;
    private String portrait;
    private int participantCount;
}
