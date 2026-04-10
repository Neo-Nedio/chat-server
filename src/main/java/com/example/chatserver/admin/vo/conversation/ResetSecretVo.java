package com.example.chatserver.admin.vo.conversation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ResetSecretVo {
    @NotNull(message = "会话不能为空~")
    private String conversationId;
}
