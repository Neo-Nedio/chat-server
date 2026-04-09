package com.example.chatserver.vo.chatGroup;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UpdateChatGroupNameVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "群名称不能为空~")
    private String name;
}
