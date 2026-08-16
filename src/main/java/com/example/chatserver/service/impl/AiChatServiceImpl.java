package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.config.AiConfig;
import com.example.chatserver.constant.AiChatRole;
import com.example.chatserver.dto.LlmMessageDto;
import com.example.chatserver.entity.AiChatRecord;
import com.example.chatserver.entity.AiModel;
import com.example.chatserver.mapper.AiChatRecordMapper;
import com.example.chatserver.service.AiChatService;
import com.example.chatserver.service.AiModelService;
import com.example.chatserver.vo.ai.AiChatAnswersVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiChatServiceImpl extends ServiceImpl<AiChatRecordMapper, AiChatRecord> implements AiChatService {

    @Resource
    AiModelService aiModelService;

    @Resource
    AiConfig aiConfig;

    @Override
    public List<AiChatRecord> recordList(String userId) {
        LambdaQueryWrapper<AiChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiChatRecord::getUserId, userId)
                .orderByAsc(AiChatRecord::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public AiChatRecord answers(String userId, AiChatAnswersVo aiChatAnswersVo) {
        AiModel model = aiModelService.getOwnedModel(userId, aiChatAnswersVo.getModelId());
        List<LlmMessageDto> messages = buildMessages(userId, model, aiChatAnswersVo.getQuestion());
        String content = aiModelService.chat(model, messages);
        return saveRecord(userId, model.getId(), AiChatRole.Assistant, content);
    }

    @Override
    public void answersStream(String userId, AiChatAnswersVo aiChatAnswersVo, SseEmitter emitter) {
        try {
            AiModel model = aiModelService.getOwnedModel(userId, aiChatAnswersVo.getModelId());
            List<LlmMessageDto> messages = buildMessages(userId, model, aiChatAnswersVo.getQuestion());
            StringBuilder fullContent = new StringBuilder();
            //  调用 AI 流式接口
            aiModelService.chatStream(model, messages, delta -> {
                fullContent.append(delta);
                sendEvent(emitter, "delta", Map.of("content", delta));
            });
            // 保存对话记录
            AiChatRecord record = saveRecord(userId, model.getId(), AiChatRole.Assistant, fullContent.toString());

            // 推送完成事件
            sendEvent(emitter, "done", record);
            emitter.complete();

        } catch (Exception e) {
            log.error("AI 流式问答失败", e);
            try {
                sendEvent(emitter, "error", Map.of("msg", String.valueOf(e.getMessage())));
            } catch (IOException ignored) {
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * 组装 LLM 消息列表：短期记忆 → 用户问题
     * （长期记忆待向量库选型后补充，位置在记忆之前）
     */
    private List<LlmMessageDto> buildMessages(String userId, AiModel model, String question) {
        List<LlmMessageDto> messages = new ArrayList<>();
        //短期记忆：当前用户最近 N 条记录（与用户绑定，不限定模型），role 列直接可用
        LambdaQueryWrapper<AiChatRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiChatRecord::getUserId, userId)
                .orderByDesc(AiChatRecord::getCreateTime)
                .last("LIMIT " + aiConfig.getShortMemorySize());
        List<AiChatRecord> history = list(queryWrapper);
        Collections.reverse(history);
        for (AiChatRecord record : history) {
            messages.add(new LlmMessageDto(record.getRole(), record.getContent()));
        }
        messages.add(LlmMessageDto.user(question));
        //问题落库（必须在取短期记忆之后，否则本轮问题会在上下文里出现两次）
        saveRecord(userId, model.getId(), AiChatRole.User, question);
        return messages;
    }

    /**
     * 聊天记录落库
     */
    private AiChatRecord saveRecord(String userId, String modelId, String role, String content) {
        AiChatRecord record = new AiChatRecord();
        record.setId(IdUtil.randomUUID());
        record.setUserId(userId);
        record.setModelId(modelId);
        record.setRole(role);
        record.setContent(content);
        save(record);
        return record;
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException {
        emitter.send(
                SseEmitter.event()
                .name(name) // 事件名称: delta / done / error
                .data(data)); // 事件数据
    }
}