package com.example.chatserver.dto;

import lombok.Data;

import java.util.List;


@Data
public class CheckUploadResultDto {

    /**
     * 是否已上传（true表示秒传成功，无需再传）
     */
    private Boolean uploaded;

    /**
     * 已上传的分片序号（断点续传用）
     */
    private List<String> uploadedChunks;

    public CheckUploadResultDto(Boolean uploaded, List<String> uploadedChunks) {
        this.uploaded = uploaded;
        this.uploadedChunks = uploadedChunks;
    }
}
