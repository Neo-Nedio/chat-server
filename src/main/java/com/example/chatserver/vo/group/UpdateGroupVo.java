package com.example.chatserver.vo.group;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UpdateGroupVo {

    @NotNull(message = "分组名称分组id")
    private String groupId;

    @NotNull(message = "分组名称不能为空")
    private String groupName;
}
