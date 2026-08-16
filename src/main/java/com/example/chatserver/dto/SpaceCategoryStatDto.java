package com.example.chatserver.dto;

import lombok.Data;


@Data
public class SpaceCategoryStatDto {

    /**
     * 文件分类
     */
    private String fileCategory;

    /**
     * 文件数量
     */
    private Long fileCount;

    /**
     * 总大小（字节）
     */
    private Long totalSize;

    public SpaceCategoryStatDto() {
    }

    public SpaceCategoryStatDto(String fileCategory, Long fileCount, Long totalSize) {
        this.fileCategory = fileCategory;
        this.fileCount = fileCount;
        this.totalSize = totalSize;
    }
}
