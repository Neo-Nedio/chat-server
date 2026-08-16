package com.example.chatserver.vo.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UpdateAiModelVo {

    @NotNull(message = "模型配置id不能为空")
    private String id;

    private String modelName;

    private String baseUrl;

    private String apiKey;

    private String model;
}