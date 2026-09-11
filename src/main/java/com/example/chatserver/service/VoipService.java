package com.example.chatserver.service;

import com.example.chatserver.dto.voip.CallInviteDto;
import com.example.chatserver.dto.voip.LiveKitRoomUserDto;
import com.example.chatserver.vo.voip.GroupCallHangupVo;
import com.example.chatserver.vo.voip.GroupCallInviteVo;
import com.example.chatserver.vo.voip.LiveKitTokenVo;

import java.util.List;

public interface VoipService {
    CallInviteDto inviteGroup(String userId, GroupCallInviteVo vo);

    void hangupGroup(String userId, GroupCallHangupVo vo);

    String getLiveKitHost();

    String getGroupToken(String userId, LiveKitTokenVo vo);

    List<LiveKitRoomUserDto> getRoomUsers(String userId, LiveKitTokenVo vo);
}
