package com.example.chatserver.dto;

import com.example.chatserver.entity.Notify;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友申请 / 入群申请 通用通知 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApplyNotifyDto extends Notify {
    /** 发起方名称（申请人） */
    private String fromName;
    /** 发起方头像（申请人） */
    private String fromPortrait;
    /** 接收方名称（好友通知=对方用户名；群聊通知=群名称） */
    private String toName;
    /** 接收方头像（好友通知=对方头像；群聊通知=群头像） */
    private String toPortrait;
}
