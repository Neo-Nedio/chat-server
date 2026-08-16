package com.example.chatserver.vo.cloudDrive;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UploadCheckVo {

    @NotBlank(message = "文件hash不能为空~")
    private String fileHash;

    @NotNull(message = "文件大小不能为空~")
    private Long fileSize;

    @NotBlank(message = "文件名不能为空~")
    private String fileName;

    @NotNull(message = "分片总数不能为空~")
    private Integer totalChunk;
}
