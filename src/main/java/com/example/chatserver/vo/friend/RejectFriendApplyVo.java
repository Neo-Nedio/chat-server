package com.example.chatserver.vo.friend;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class RejectFriendApplyVo {

    @NotNull(message = "fromId不能为空")
    private String fromId;

}
