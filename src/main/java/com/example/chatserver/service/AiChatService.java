package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.AiChatRecord;
import com.example.chatserver.vo.ai.AiChatAnswersVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


public interface AiChatService extends IService<AiChatRecord> {

    /**
     * 我的AI聊天记录（按时间正序）
     */
    List<AiChatRecord> recordList(String userId);

    /**
     * AI 问答（同步），返回落库后的回答记录
     */
    AiChatRecord answers(String userId, AiChatAnswersVo aiChatAnswersVo);

    /**
     * AI 问答（SSE 流式），事件：delta / done / error
     */
    void answersStream(String userId, AiChatAnswersVo aiChatAnswersVo, SseEmitter emitter);
}