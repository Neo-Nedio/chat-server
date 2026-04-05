package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.FriendNotifyDto;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.notify.FriendApplyNotifyVo;
import com.example.chatserver.vo.notify.ReadNotifyVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;

    /**
     * 好友通知列表
     */
    @GetMapping("/friend/list")
    public JSONObject friendListNotify(@Userid String userId) {
        List<FriendNotifyDto> result = notifyService.friendListNotify(userId);
        return ResultUtil.Succeed(result);
    }


    /**
     * 好友申请通知
     */
    @PostMapping("/friend/apply")
    public JSONObject friendApplyNotify(@Userid String userId, @RequestBody FriendApplyNotifyVo friendApplyNotifyVo) {
        boolean result = notifyService.friendApplyNotify(userId, friendApplyNotifyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 通知已读
     */
    @PostMapping("/read")
    public JSONObject readNotify(@Userid String userId, @RequestBody ReadNotifyVo readNotifyVo) {
        boolean result = notifyService.readNotify(userId, readNotifyVo);
        return ResultUtil.Succeed(result);
    }
}

