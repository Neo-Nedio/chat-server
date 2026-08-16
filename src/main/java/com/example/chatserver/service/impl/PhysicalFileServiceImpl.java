package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.PhysicalFile;
import com.example.chatserver.mapper.PhysicalFileMapper;
import com.example.chatserver.service.PhysicalFileService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class PhysicalFileServiceImpl extends ServiceImpl<PhysicalFileMapper, PhysicalFile> implements PhysicalFileService {

    @Resource
    MinioUtil minioUtil;

    @Override
    public PhysicalFile getByHash(String fileHash) {
        LambdaQueryWrapper<PhysicalFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PhysicalFile::getFileHash, fileHash);
        return getOne(queryWrapper);
    }

    @Override
    public PhysicalFile createFile(UploadCheckVo uploadCheckVo, String storagePath) {
        PhysicalFile file = new PhysicalFile();
        file.setId(IdUtil.simpleUUID());
        file.setFileHash(uploadCheckVo.getFileHash());
        file.setFileSize(uploadCheckVo.getFileSize());
        file.setStoragePath(storagePath);
        //refCount不设置，落库表默认值0
        try {
            save(file);
            return file;
        } catch (DuplicateKeyException e) {
            //并发合并同一hash时唯一键兜底，直接复用已有物理文件
            return getByHash(uploadCheckVo.getFileHash());
        }
    }

    @Override
    public void cleanUnreferencedFiles() {
        List<PhysicalFile> zeroRefs = baseMapper.listZeroRefBeforeOneDay();
        if (zeroRefs == null || zeroRefs.isEmpty()) {
            return;
        }
        for (PhysicalFile file : zeroRefs) {
            //守卫删除：删除瞬间若有秒传把计数加回去则affected=0，跳过该条（记录与对象都保留）
            if (baseMapper.deleteZeroRefById(file.getId()) <= 0) {
                continue;
            }
            //记录已删，此后同内容再上传会重新走完整上传；删MinIO对象失败仅留无引用的孤儿对象，记录日志即可
            minioUtil.removeFile(file.getStoragePath());
        }
        log.info("清理引用为0的物理文件完成, 共{}个", zeroRefs.size());
    }
}
