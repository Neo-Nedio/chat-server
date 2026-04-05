package com.example.chatserver.vo.notify;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class FriendApplyNotifyVo {

    @NotNull(message = "用户不能为空")
    private String userId;

    private String content;
}
