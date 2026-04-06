package com.example.chatserver.vo.friend;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CareForFriendVo {

    @NotNull(message = "好友不能为空")
    private String friendId;
}
