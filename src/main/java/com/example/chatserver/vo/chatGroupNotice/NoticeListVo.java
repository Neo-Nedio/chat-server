package com.example.chatserver.vo.chatGroupNotice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class NoticeListVo {
    @NotNull(message = "群不能为空~")
    private String groupId;
}
