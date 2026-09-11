package com.example.chatserver.vo.voip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class GroupCallHangupVo {
    @NotBlank
    private String groupId;
    @NotEmpty
    private List<@NotBlank String> userIds;
}
