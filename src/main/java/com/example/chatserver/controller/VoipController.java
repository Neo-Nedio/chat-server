package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.voip.CallInviteDto;
import com.example.chatserver.dto.voip.LiveKitRoomUserDto;
import com.example.chatserver.service.VoipService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.voip.GroupCallHangupVo;
import com.example.chatserver.vo.voip.GroupCallInviteVo;
import com.example.chatserver.vo.voip.LiveKitTokenVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/voip")
public class VoipController {
    @Resource
    VoipService voipService;

    @PostMapping("/call/group/invite")
    public JSONObject invite(@Userid String userId, @Valid @RequestBody GroupCallInviteVo vo) {
        return ResultUtil.Succeed(voipService.inviteGroup(userId, vo));
    }
    @PostMapping("/call/group/hangup")
    public JSONObject hangup(@Userid String userId, @Valid @RequestBody GroupCallHangupVo vo) {
        voipService.hangupGroup(userId, vo);
        return ResultUtil.Succeed();
    }
    @PostMapping("/livekit/host")
    public JSONObject host() {
        return ResultUtil.Succeed(voipService.getLiveKitHost());
    }
    @PostMapping("/livekit/token/group")
    public JSONObject token(@Userid String userId, @Valid @RequestBody LiveKitTokenVo vo) {
        return ResultUtil.Succeed(voipService.getGroupToken(userId, vo));
    }
    @PostMapping("/livekit/room/users")
    public JSONObject roomUsers(@Userid String userId, @Valid @RequestBody LiveKitTokenVo vo) {
        List<LiveKitRoomUserDto> result = voipService.getRoomUsers(userId, vo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/livekit/token/live/start")
    public JSONObject startLive(@Userid String userId) {
        return ResultUtil.Succeed(voipService.startLive(userId));
    }

    @PostMapping("/livekit/token/live/get")
    public JSONObject getLiveToken(@Userid String userId, @Valid @RequestBody LiveKitTokenVo vo) {
        return ResultUtil.Succeed(voipService.getLiveToken(userId, vo));
    }
}
