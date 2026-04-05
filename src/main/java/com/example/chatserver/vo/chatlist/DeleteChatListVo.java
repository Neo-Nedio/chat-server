package com.example.chatserver.vo.chatlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DeleteChatListVo {
    @NotNull(message = "会话id不能为空")
    private String chatListId;
}
