package com.example.chatserver.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
public class SystemMsgDto {
    @AllArgsConstructor
    @Data
    static
    class Content {
        private boolean isEmphasize;  // 是否强调
        private String content;       // 文本内容
    }

    private ArrayList<Content> contents;

    public SystemMsgDto() {
        contents = new ArrayList<>();
    }

    public SystemMsgDto addEmphasizeContent(String content) {
        contents.add(new Content(true, content));
        return this;
    }

    public SystemMsgDto addContent(String content) {
        contents.add(new Content(false, content));
        return this;
    }
}
