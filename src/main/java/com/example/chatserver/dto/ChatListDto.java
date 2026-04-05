package com.example.chatserver.dto;

import com.example.chatserver.entity.ChatList;
import lombok.Data;

import java.util.List;

@Data
public class ChatListDto {
    private List<ChatList> tops;
    private List<ChatList> others;
}
