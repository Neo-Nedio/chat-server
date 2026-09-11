package com.example.chatserver.vo.voip;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LiveKitTokenVo {
    @NotBlank
    private String sessionId;
}
