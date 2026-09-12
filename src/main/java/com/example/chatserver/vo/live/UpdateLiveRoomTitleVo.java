package com.example.chatserver.vo.live;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLiveRoomTitleVo {
    @NotBlank(message = "直播间标题不能为空")
    @Size(max = 20, message = "直播间标题不能超过20个字符")
    private String title;
}
