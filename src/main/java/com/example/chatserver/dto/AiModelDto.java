package com.example.chatserver.dto;

import lombok.Data;

import java.util.Date;


/**
 * 模型配置对外视图（不含 apiKey）
 */
@Data
public class AiModelDto {

    private String id;

    private String modelName;

    private String baseUrl;

    private String model;

    private Date createTime;
}