package com.example.chatserver.admin.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.example.chatserver.admin.vo.notify.DeleteNotifyVo;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.ResultUtil;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminNotifyController")
@RequestMapping("/admin/v1/api/notify")
@Slf4j
public class NotifyController {

    @Resource
    NotifyService notifyService;

    @Resource
    MinioUtil minioUtil;


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

    /**
     * 系统通知创建
     */
    @PostMapping("/system/create")
    @UrlResource("admin")
    public JSONObject createNotify(@NotNull(message = "图片不能为空~") @RequestParam("file") MultipartFile file,
                                   @NotNull(message = "标题不能为空~") @RequestParam("title") String title,
                                   @NotNull(message = "内容不能为空~") @RequestParam("text") String text) {
        String fileName = "notify/" + IdUtil.randomUUID();
        try {
            minioUtil.upload(file.getInputStream(), fileName, file.getContentType(), file.getSize());
        } catch (Exception e) {
            throw new BaseException("图片上传失败~");
        }
        boolean result = notifyService.createNotify(fileName, title, text);
        return ResultUtil.ResultByFlag(result);
    }
}
