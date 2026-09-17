package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.LiveRoomService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.dto.voip.LiveDanmakuDto;
import com.example.chatserver.vo.live.PublishDanmakuVo;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/live-room")
public class LiveRoomController {
    @Resource
    LiveRoomService liveRoomService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

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

    @GetMapping("/get/background")
    public JSONObject getBackground(@Userid String userId,
                                    @RequestParam("fileName") String fileName) {
        String url = (String) redisUtils.get(fileName);
        if (url == null || url.isBlank()) {
            url = minioUtil.preview(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }

    /**
     * 获取直播间弹幕历史记录。
     */
    @GetMapping("/danmaku/list")
    public JSONObject danmakuList(@RequestParam("sessionId") String sessionId) {
        List<LiveDanmakuDto> result = liveRoomService.getDanmakuList(sessionId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 发布弹幕，同时保存到 Redis 并广播到 LiveKit 房间。
     */
    @PostMapping("/danmaku/send")
    public JSONObject sendDanmaku(@Userid String userId,
                                  @Valid @RequestBody PublishDanmakuVo vo) {
        LiveDanmakuDto result = liveRoomService.publishDanmaku(userId, vo);
        return ResultUtil.Succeed(result);
    }

}
