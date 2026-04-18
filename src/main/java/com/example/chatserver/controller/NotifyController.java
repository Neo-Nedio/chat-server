package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.UserRole;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.FriendNotifyDto;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.notify.FriendApplyNotifyVo;
import com.example.chatserver.vo.notify.GroupApplyNotifyVo;
import com.example.chatserver.vo.notify.ReadNotifyVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

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
    public JSONObject friendApplyNotify(@Userid String userId,@UserRole String userRole,@Valid @RequestBody FriendApplyNotifyVo friendApplyNotifyVo) {
        boolean result = notifyService.friendApplyNotify(userId,userRole, friendApplyNotifyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 群聊申请通知
     */
    @PostMapping("/group/apply")
    public JSONObject groupApplyNotify(@Userid String userId,@UserRole String userRole,@Valid @RequestBody GroupApplyNotifyVo groupApplyNotifyVo) {
        boolean result = notifyService.groupApplyNotify(userId,userRole, groupApplyNotifyVo);
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

    /**
     * 系统通知列表
     */
    @GetMapping("/system/list")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 最新系统通知
     */
    @GetMapping("/system/latest")
    public JSONObject SystemNotifyLatest(@Userid String userId) {
        SystemNotifyDto result = notifyService.SystemNotifyLatest(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 系统通知已读
     */
    @GetMapping("/system/read")
    public JSONObject SystemNotifyRead(@Userid String userId) {
        boolean result = notifyService.SystemNotifyRead(userId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 通知图片获取
     */
    @GetMapping("/get/img")
    public JSONObject getImg(@NotNull(message = "图片名字不能为空~") @RequestParam("fileName") String fileName) {
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.preview(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }
}

