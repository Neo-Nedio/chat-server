package com.example.chatserver.dto;

import com.example.chatserver.entity.ChatGroup;
import lombok.Data;

import java.util.List;


@Data
public class ChatDto {
    private List<FriendDetailsDto> friend;
    private List<ChatGroup> group;
}
