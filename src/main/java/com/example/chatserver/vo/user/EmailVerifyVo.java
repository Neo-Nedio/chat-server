package com.example.chatserver.vo.user;

import jakarta.validation.constraints.Email;
import lombok.Data;


@Data
public class EmailVerifyVo {
    @Email(message = "邮箱格式有误~")
    private String email;
}
