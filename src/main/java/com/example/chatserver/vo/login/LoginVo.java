package com.example.chatserver.vo.login;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class LoginVo {
    @NotNull(message = "账号不能为空")
    private String account;
    @NotNull(message = "密码不能为空")
    private String password;

    // 登录设备
    private String onlineEquipment;

    // Pushy 设备推送标识
    private String pushyToken;
}
