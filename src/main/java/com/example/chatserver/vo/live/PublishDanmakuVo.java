package com.example.chatserver.vo.live;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublishDanmakuVo {
    @NotBlank(message = "直播房间不能为空")
    private String sessionId;

    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 200, message = "弹幕内容不能超过200个字符")
    private String content;
}
