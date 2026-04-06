package com.example.chatserver.vo.talk;

import lombok.Data;

import java.util.List;

@Data
public class CreateTalkVo {
    private String text;
    private List<String> permission;
}
