package com.example.chatserver.vo.message;

import lombok.Data;

@Data
public class RetractionMsgVo {
    private String msgId;
    private String targetId;
}
