package com.example.chatserver.vo.cloudDrive;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;


@Data
public class FileDeleteVo {

    @NotEmpty(message = "文件id列表不能为空~")
    private List<String> spaceFileIds;
}
