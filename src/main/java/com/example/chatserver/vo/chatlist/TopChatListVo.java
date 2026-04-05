package com.example.chatserver.vo.chatlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TopChatListVo {
    @NotNull(message = "会话id不能为空")
    private String chatListId;

    @NotNull(message = "是否置顶不能为空")
    private boolean isTop;

    public void setIsTop(boolean top) {
        isTop = top;
    }
}
