package com.example.chatserver.vo.chatlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateChatListVo {
    //好友
    @NotNull(message = "好友id不能为空")
    private String toId;
    private String type = "user";
}
