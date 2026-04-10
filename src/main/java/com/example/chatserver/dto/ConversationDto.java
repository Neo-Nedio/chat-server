package com.example.chatserver.dto;

import com.example.chatserver.entity.Conversation;
import lombok.Data;

@Data
public class ConversationDto extends Conversation {
    private String name;
    private String portrait;
    private String account;
}
