package com.example.chatserver.vo.notify;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupApplyNotifyVo {

    @NotNull(message = "群聊不能为空")
    private String groupId;

    private String content;
}
