package com.example.chatserver.dto.voip;

import lombok.Data;

import java.util.Date;

@Data
public class LiveRoomInfoDto {
    private String id;
    private String userId;
    private String title;
    private String background;
    private String portrait;
    private Date createTime;
    private Date updateTime;
}
