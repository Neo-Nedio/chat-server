package com.example.chatserver.admin.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminNotifyController")
@RequestMapping("/admin/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;


    @GetMapping("/system/list")
    @UrlResource("admin")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }
}
