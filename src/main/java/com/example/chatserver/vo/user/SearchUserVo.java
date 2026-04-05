package com.example.chatserver.vo.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class SearchUserVo {
    @NotNull(message = "用户信息不能为空")
    private String userInfo;
}
