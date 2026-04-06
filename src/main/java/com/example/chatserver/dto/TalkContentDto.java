package com.example.chatserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //忽略未知的 JSON 属性
@JsonInclude(JsonInclude.Include.NON_NULL) //控制 null 值是否序列化
public class TalkContentDto {
    private String text;
    private List<String> img;
}
