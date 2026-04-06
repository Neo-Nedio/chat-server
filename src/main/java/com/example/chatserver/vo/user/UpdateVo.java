package com.example.chatserver.vo.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateVo {
    @NotNull(message = "用户名不能为空")
    private String name;
    private String sex;
    private Date birthday;
    private String signature;
    @NotNull(message = "头像不能为空")
    private String portrait;
}
