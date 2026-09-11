package com.example.chatserver.vo.voip;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class GroupCallInviteVo {
    @NotBlank
    private String groupId;
    private List<@NotBlank String> userIds;
    @NotBlank
    private String callType;
}
