package com.example.chatserver.dto;

import com.example.chatserver.entity.User;
import lombok.Data;

@Data
public class Top10MsgDto extends User {
    private int num;
}
