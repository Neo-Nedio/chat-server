package com.example.chatserver.admin.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.admin.vo.notify.DeleteNotifyVo;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminNotifyController")
@RequestMapping("/admin/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;


    /**
     * 系统通知列表
     */
    @GetMapping("/system/list")
    @UrlResource("admin")
    public JSONObject SystemListNotify(@Userid String userId) {
        List<SystemNotifyDto> result = notifyService.SystemListNotify(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 系统通知删除
     */
    @PostMapping("/system/delete")
    @UrlResource("admin")
    public JSONObject deleteNotify(@RequestBody DeleteNotifyVo deleteNotifyVo) {
        boolean result = notifyService.deleteNotify(deleteNotifyVo);
        return ResultUtil.ResultByFlag(result);
    }
}
