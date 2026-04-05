package com.example.chatserver.vo.group;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DeleteGroupVo {
    @NotNull(message = "分组名称分组id")
    private String groupId;
}
