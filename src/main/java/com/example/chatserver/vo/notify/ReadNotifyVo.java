package com.example.chatserver.vo.notify;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class ReadNotifyVo {

    @NotNull(message = "通知类型")
    private String notifyType;
}
