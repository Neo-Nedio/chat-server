package com.example.chatserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * LLM 对话消息（OpenAI 兼容格式）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmMessageDto {

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    public static LlmMessageDto system(String content) {
        return new LlmMessageDto("system", content);
    }

    public static LlmMessageDto user(String content) {
        return new LlmMessageDto("user", content);
    }

    public static LlmMessageDto assistant(String content) {
        return new LlmMessageDto("assistant", content);
    }
}