package com.example.chatserver.vo.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class ForgetVo {
    @NotNull(message = "账号不能为空")
    private String account;
    @NotNull(message = "密码不能为空")
    private String password;
    @Email(message = "邮箱格式有误")
    private String email;
    @NotNull(message = "验证码不能为空")
    private String code;
}
