package com.example.chatserver.vo.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AddAiModelVo {

    @NotNull(message = "显示名不能为空")
    private String modelName;

    @NotNull(message = "接口地址不能为空")
    private String baseUrl;

    @NotNull(message = "ApiKey不能为空")
    private String apiKey;

    @NotNull(message = "模型标识不能为空")
    private String model;
}