package com.example.chatserver.dto.voip;

import lombok.Data;

@Data
public class LiveKitRoomUserDto {
    private String userId;
    private String username;
    private String avatar;
    private String state;
}
