package com.example.chatserver.admin.vo.expose;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThirdSendMsgVo {
    @NotNull(message = "邮箱不能为空~")
    private String email;
    private String content;
}
