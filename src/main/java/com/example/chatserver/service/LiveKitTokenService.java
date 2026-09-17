package com.example.chatserver.service;

import com.example.chatserver.dto.voip.LiveKitRoomUserDto;

import java.util.List;

public interface LiveKitTokenService {
    String getHost();

    String createToken(String userId, String sessionId);

    String createToken(String userId, String sessionId, boolean canPublish);

    List<LiveKitRoomUserDto> listParticipants(String sessionId);

    void sendData(String sessionId, String data, String topic);

    List<String> listActiveLiveRooms();
}
