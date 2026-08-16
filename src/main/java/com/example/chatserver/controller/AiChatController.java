package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.entity.AiChatRecord;
import com.example.chatserver.service.AiChatService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.ai.AiChatAnswersVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/v1/api/ai/chat")
public class AiChatController {

    @Resource
    AiChatService aiChatService;

    /**
     * 我的AI聊天记录
     */
    @GetMapping("/record/list")
    public JSONObject recordList(@Userid String userId) {
        List<AiChatRecord> list = aiChatService.recordList(userId);
        return ResultUtil.Succeed(list);
    }

    /**
     * AI 问答（同步）
     */
    @PostMapping("/answers")
    public JSONObject answers(@Userid String userId, @Valid @RequestBody AiChatAnswersVo aiChatAnswersVo) {
        AiChatRecord record = aiChatService.answers(userId, aiChatAnswersVo);
        return ResultUtil.Succeed(record);
    }

    /**
     * AI 问答（SSE 流式），事件：delta / done / error
     */
    @PostMapping(value = "/answers/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) //声明响应格式为 SSE
    public SseEmitter answersStream(@Userid String userId, @Valid @RequestBody AiChatAnswersVo aiChatAnswersVo) {
        SseEmitter emitter = new SseEmitter(180_000L);  // 超时 180 秒
        //异步执行，立即返回 emitter 建立响应，delta 才能实时下发
        CompletableFuture.runAsync(() -> aiChatService.answersStream(userId, aiChatAnswersVo, emitter));
        return emitter;
    }
}