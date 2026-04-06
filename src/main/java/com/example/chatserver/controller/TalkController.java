package com.example.chatserver.controller;


import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.TalkListDto;
import com.example.chatserver.entity.Talk;
import com.example.chatserver.service.TalkService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.talk.CreateTalkVo;
import com.example.chatserver.vo.talk.DeleteTalkVo;
import com.example.chatserver.vo.talk.TalkListVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/talk")
public class TalkController {

    @Resource
    TalkService talkService;

    @Resource
    MinioUtil minioUtil;

    @PostMapping("/list")
    public JSONObject talkList(@Userid String userId, @RequestBody TalkListVo talkListVo) {
        List<TalkListDto> result = talkService.talkList(userId, talkListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/create")
    public JSONObject createTalk(@Userid String userId, @RequestBody CreateTalkVo createTalkVo) {
        Talk result = talkService.createTalk(userId, createTalkVo);
        return ResultUtil.Succeed(result);
    }


    @PostMapping("/upload/img")
    public JSONObject uploadImgTalk(HttpServletRequest request,
                                    @Userid String userId,
                                    @RequestHeader("talkId") String talkId,
                                    @RequestHeader("name") String name,
                                    @RequestHeader("type") String type,
                                    @RequestHeader("size") long size) throws IOException {
        String imgName = IdUtil.randomUUID() + name.substring(name.lastIndexOf("."));
        String imgPath = userId + "/img/" + imgName;
        minioUtil.uploadFile(request.getInputStream(), imgPath, size);
        Talk talk = talkService.updateTalkImg(userId, talkId, imgName);
        return ResultUtil.Succeed(talk);
    }

    @PostMapping("/delete")
    public JSONObject deleteTalk(@Userid String userId, @RequestBody DeleteTalkVo deleteTalkVo) {
        boolean result = talkService.deleteTalk(userId, deleteTalkVo);
        return ResultUtil.ResultByFlag(result);
    }
}

