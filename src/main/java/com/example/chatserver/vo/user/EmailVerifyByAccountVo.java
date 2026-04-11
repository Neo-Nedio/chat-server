package com.example.chatserver.vo.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class EmailVerifyByAccountVo {
    @NotNull(message = "账号不能为空~")
    private String account;
}
