package com.example.chatserver.admin.vo.notify;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DeleteNotifyVo {
    @NotNull(message = "通知不能为空")
    private String notifyId;
}
