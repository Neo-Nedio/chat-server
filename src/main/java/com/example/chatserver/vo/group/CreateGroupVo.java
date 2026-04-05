package com.example.chatserver.vo.group;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateGroupVo {

    @NotNull(message = "分组名称不能为空")
    private String groupName;
}
