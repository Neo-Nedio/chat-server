package com.example.chatserver.vo.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DeleteAiModelVo {

    @NotNull(message = "模型配置id不能为空")
    private String id;
}