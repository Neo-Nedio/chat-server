package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.PhysicalFile;
import com.example.chatserver.entity.Space;
import com.example.chatserver.entity.SpaceFile;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;


public interface SpaceFileService extends IService<SpaceFile> {

    /**
     * 基于已有物理文件创建逻辑文件（秒传/合并后调用）：建文件 + 物理引用计数加1 + 空间已用容量累加
     */
    void createUserFileFromPhysicalFile(Space space, UploadCheckVo uploadCheckVo, PhysicalFile physicalFile);
}
