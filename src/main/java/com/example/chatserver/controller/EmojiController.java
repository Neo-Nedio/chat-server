package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.EmojiService;
import com.example.chatserver.utils.ResultUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/api/emoji")
public class EmojiController {

    @Resource
    EmojiService emojiService;


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
}
