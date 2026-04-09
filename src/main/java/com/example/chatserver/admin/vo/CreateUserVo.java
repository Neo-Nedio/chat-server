package com.example.chatserver.admin.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateUserVo {
    @NotNull(message = "账号不能为空")
    private String account;
    private String username;
    @Email(message = "邮箱格式有误")
    private String email;
    private String phone;
}
