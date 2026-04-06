package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.LikeListDto;
import com.example.chatserver.service.TalkLikeService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.talkLike.CreateTalkLikeVo;
import com.example.chatserver.vo.talkLike.DeleteTalkLikeVo;
import com.example.chatserver.vo.talkLike.TalkLikeListVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/v1/api/talk-like")
public class TalkLikeController {
    @Resource
    TalkLikeService talkLikeService;

    @PostMapping("/create")
    public JSONObject createTalkLike(@Userid String userId, @RequestBody CreateTalkLikeVo createTalkLikeVo) {
        boolean result = talkLikeService.createTalkLike(userId, createTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkLikeList(@Userid String userId, @RequestBody TalkLikeListVo talkLikeListVo) {
        List<LikeListDto> result = talkLikeService.talkLikeList(userId, talkLikeListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkLike(@Userid String userId, @RequestBody DeleteTalkLikeVo deleteTalkLikeVo) {
        boolean result = talkLikeService.deleteTalkLike(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

