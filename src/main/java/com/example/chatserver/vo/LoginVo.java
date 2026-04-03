package com.example.chatserver.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class LoginVo {
    @NotNull(message = "账号不能为空")
    private String account;
    @NotNull(message = "密码不能为空")
    private String password;
}
