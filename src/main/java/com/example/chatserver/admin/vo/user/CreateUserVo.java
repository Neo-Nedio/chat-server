package com.example.chatserver.admin.vo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateUserVo {
    @NotNull(message = "账号不能为空")
    private String account;
    @NotNull(message = "用户名不能为空")
    private String name;
    @Email(message = "邮箱格式有误")
    @Email(message = "邮箱不能为空")
    private String email;
    private String phone;
}
