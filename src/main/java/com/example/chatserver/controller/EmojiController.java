package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.EmojiService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping("/v1/api/emoji")
public class EmojiController {

    @Resource
    EmojiService emojiService;


    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;


    /**
     * 获取表情列表
     */
    @GetMapping("/list")
    public JSONObject getEmojiList(@Userid String userId) {
        return ResultUtil.Succeed(emojiService.list(userId));
    }

    /**
     * 添加表情列表
     */
    @PostMapping("/add")
    public JSONObject add(@Userid String userId, @RequestParam("emoji") String emoji) {
        return ResultUtil.ResultByFlag(emojiService.add(userId,emoji));
    }

    @PostMapping(value = "upload")
    public JSONObject uploadFrom(@Userid String userId,
                                 @RequestParam("name") String name,
                                 @RequestParam("type") String type,
                                 @RequestParam("size") long size,
                                 @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new  BaseException("文件不能为空");
        }
        String fileName = userId + "-emoji-" + System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        minioUtil.upload(file.getInputStream(), fileName, type, size);
        emojiService.add(userId,fileName);
        return ResultUtil.Succeed(fileName);
    }

    /**
     * 获取图片
     */
    @GetMapping("/get")
    public JSONObject get(@Userid String userId, @RequestParam("fileName") String fileName) {
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.preview(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }
}
