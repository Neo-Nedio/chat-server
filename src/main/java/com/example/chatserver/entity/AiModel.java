package com.example.chatserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ai_model")
public class AiModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 归属用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 显示名（用户自定义）
     */
    @TableField("model_name")
    private String modelName;

    /**
     * OpenAI 兼容接口地址，如 https://api.openai.com/v1
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * 模型ApiKey（敏感，不对外返回）
     */
    @JsonIgnore
    @TableField("api_key")
    private String apiKey;

    /**
     * 模型标识，如 gpt-4o-mini
     */
    @TableField("model")
    private String model;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}