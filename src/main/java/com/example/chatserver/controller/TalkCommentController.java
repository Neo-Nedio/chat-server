package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.CommentListDto;
import com.example.chatserver.service.TalkCommentService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.talkComment.CreateTalkCommentVo;
import com.example.chatserver.vo.talkComment.DeleteTalkCommentVo;
import com.example.chatserver.vo.talkComment.TalkCommentListVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/api/talk-comment")
public class TalkCommentController {
    @Resource
    TalkCommentService talkCommentService;

    @PostMapping("/create")
    public JSONObject createTalkComment(@Userid String userId, @RequestBody CreateTalkCommentVo createTalkCommentVo) {
        boolean result = talkCommentService.createTalkComment(userId, createTalkCommentVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/list")
    public JSONObject talkCommentList(@Userid String userId, @RequestBody TalkCommentListVo talkCommentListVo) {
        List<CommentListDto> result = talkCommentService.talkCommentList(userId, talkCommentListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalkComment(@Userid String userId, @RequestBody DeleteTalkCommentVo deleteTalkLikeVo) {
        boolean result = talkCommentService.deleteTalkComment(userId, deleteTalkLikeVo);
        return ResultUtil.ResultByFlag(result);
    }
}

