package com.example.chatserver.vo.user;

import lombok.Data;


@Data
public class UpdatePasswordVo {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
