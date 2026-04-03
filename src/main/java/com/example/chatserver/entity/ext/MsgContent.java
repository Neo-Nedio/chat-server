package com.example.chatserver.entity.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder //生成建造者模式，方便链式创建对象
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) //在反序列化（JSON → Java 对象）时，如果 JSON 中有 Java 类不认识的字段，忽略它们而不报错。
@JsonInclude(JsonInclude.Include.NON_NULL) //在序列化（Java 对象 → JSON）时，如果字段值为 null，不包含该字段
public class MsgContent implements Serializable {
    //序列号uid
    @Serial
    private static final long serialVersionUID = 1L;
    //发送方用户id
    private String fromUserId;
    //消息内容类型
    private String type;
    //消息内容
    private String content;
}
