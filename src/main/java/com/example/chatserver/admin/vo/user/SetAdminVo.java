package com.example.chatserver.admin.vo.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SetAdminVo {
    @NotNull(message = "用户不能为空")
    private String userId;
}
