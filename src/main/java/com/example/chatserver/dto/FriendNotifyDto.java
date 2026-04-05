package com.example.chatserver.dto;

import com.example.chatserver.entity.Notify;
import lombok.Data;

@Data
public class FriendNotifyDto extends Notify {
    private String fromName;
    private String fromPortrait;
    private String toName;
    private String toPortrait;
}
