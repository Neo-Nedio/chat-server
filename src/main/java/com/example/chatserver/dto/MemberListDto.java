package com.example.chatserver.dto;

import lombok.Data;

import java.util.Date;

@Data
public class MemberListDto {
    private String chatGroupId;
    private String userId;
    private String name;
    private String remark;
    private String friendId;
    private String groupName;
    private String portrait;
    private Date lastReadNoticeTime;
    private Date banEndTime;
    private String create_time;
    private String update_time;
}
