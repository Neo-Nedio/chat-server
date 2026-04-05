package com.example.chatserver.vo.friend;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AgreeFriendApplyVo {

    @NotNull(message = "好友申请的通知id")
    public String notifyId;
}
