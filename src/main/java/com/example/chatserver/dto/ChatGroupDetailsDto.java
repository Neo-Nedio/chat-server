package com.example.chatserver.dto;

import com.example.chatserver.entity.ChatGroupNotice;
import lombok.Data;

@Data
public class ChatGroupDetailsDto {
    private String id;
    private String userId;
    private String ownerUserId;
    private String portrait;
    private String name;
    private ChatGroupNotice notice;
    private String memberNum;
    private String groupName;
    private String groupRemark;
    private String createTime;
    private String updateTime;
}
