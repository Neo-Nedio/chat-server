package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.PhysicalFile;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;


public interface PhysicalFileService extends IService<PhysicalFile> {

    /**
     * 按文件hash查询物理文件（秒传检查）
     */
    PhysicalFile getByHash(String fileHash);

    /**
     * 创建物理文件记录（hash唯一，并发下重复插入时回查已有记录）
     */
    PhysicalFile createFile(UploadCheckVo uploadCheckVo, String storagePath);

    /**
     * 清理引用计数为0且已持续1天的物理文件：删除库表记录与MinIO对象（定时任务调用）
     */
    void cleanUnreferencedFiles();
}
