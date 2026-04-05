package com.example.chatserver.vo.message;

import com.example.chatserver.entity.ext.MsgContent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class SendMsgToUserVo {
    //目标用户
    @NotNull(message = "目标用户不能为空")
    private String toUserId;
    //是否显示时间
    private boolean isShowTime;
    //消息内容
    @NotNull(message = "消息内容不能为空")
    private MsgContent msgContent;
}
