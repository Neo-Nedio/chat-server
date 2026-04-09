package com.example.chatserver.vo.chatGroupNotice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CreateNoticeVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "公告内容不能为空~")
    private String content;
}
