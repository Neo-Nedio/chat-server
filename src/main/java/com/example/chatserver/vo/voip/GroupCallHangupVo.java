package com.example.chatserver.vo.voip;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupCallHangupVo {
    @NotBlank
    private String groupId;
}
