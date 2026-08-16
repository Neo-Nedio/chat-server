package com.example.chatserver.vo.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AiChatAnswersVo {

    @NotNull(message = "模型配置不能为空")
    private String modelId;

    @NotNull(message = "问题不能为空")
    private String question;
}