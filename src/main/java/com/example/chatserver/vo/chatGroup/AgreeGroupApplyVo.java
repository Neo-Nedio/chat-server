package com.example.chatserver.vo.chatGroup;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgreeGroupApplyVo {

    @NotNull(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "群聊不能为空")
    private String groupId;
}
