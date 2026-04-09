package com.example.chatserver.vo.chatGroupNotice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class DeleteNoticeVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
    @NotNull(message = "公告不能为空~")
    private String noticeId;
}
