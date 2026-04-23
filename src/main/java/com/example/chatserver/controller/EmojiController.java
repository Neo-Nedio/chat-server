package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.EmojiService;
import com.example.chatserver.utils.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
