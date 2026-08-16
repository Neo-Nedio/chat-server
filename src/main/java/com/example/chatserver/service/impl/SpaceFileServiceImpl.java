package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.CloudDriveConstant;
import com.example.chatserver.entity.PhysicalFile;
import com.example.chatserver.entity.Space;
import com.example.chatserver.entity.SpaceFile;
import com.example.chatserver.mapper.PhysicalFileMapper;
import com.example.chatserver.mapper.SpaceFileMapper;
import com.example.chatserver.mapper.SpaceMapper;
import com.example.chatserver.service.SpaceFileService;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SpaceFileServiceImpl extends ServiceImpl<SpaceFileMapper, SpaceFile> implements SpaceFileService {

    @Resource
    SpaceMapper spaceMapper;

    @Resource
    PhysicalFileMapper physicalFileMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void createUserFileFromPhysicalFile(Space space, UploadCheckVo uploadCheckVo, PhysicalFile physicalFile) {
        SpaceFile spaceFile = new SpaceFile();
        spaceFile.setId(IdUtil.simpleUUID());
        spaceFile.setSpaceId(space.getId());
        spaceFile.setPhysicalId(physicalFile.getId());
        spaceFile.setFileName(uploadCheckVo.getFileName());
        spaceFile.setFileCategory(CloudDriveConstant.fileCategoryFromExt(uploadCheckVo.getFileName()));
        spaceFile.setFileSize(uploadCheckVo.getFileSize());
        save(spaceFile);

        //物理文件引用数加1
        physicalFileMapper.incRefCount(physicalFile.getId());
        //累加空间已用容量
        spaceMapper.incUsedBytes(space.getId(), uploadCheckVo.getFileSize(), 1);
    }
}
