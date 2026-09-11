package com.example.chatserver.service;

import com.example.chatserver.dto.voip.LiveKitRoomUserDto;

import java.util.List;

public interface LiveKitTokenService {
    String getHost();

    String createToken(String userId, String sessionId);

    List<LiveKitRoomUserDto> listParticipants(String sessionId);
}
