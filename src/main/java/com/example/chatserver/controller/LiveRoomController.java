package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.LiveRoomService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1/api/live-room")
public class LiveRoomController {
    @Resource
    LiveRoomService liveRoomService;

    @GetMapping("/info")
    public JSONObject info(@Userid String userId) {
        return ResultUtil.Succeed(liveRoomService.getInfo(userId));
    }

    @PostMapping("/title")
    public JSONObject updateTitle(@Userid String userId,
                                  @Valid @RequestBody UpdateLiveRoomTitleVo vo) {
        return ResultUtil.ResultByFlag(liveRoomService.updateTitle(userId, vo));
    }

    @PostMapping("/upload/background")
    public JSONObject uploadBackground(HttpServletRequest request,
                                       @Userid String userId,
                                       @RequestHeader("name") String name,
                                       @RequestHeader("type") String type,
                                       @RequestHeader("size") long size) throws IOException {
        return ResultUtil.Succeed(liveRoomService.uploadBackground(userId, request.getInputStream(), name, type, size));
    }

}
