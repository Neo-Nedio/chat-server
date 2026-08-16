package com.example.chatserver.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.CloudDriveConstant;
import com.example.chatserver.dto.SpaceCategoryStatDto;
import com.example.chatserver.entity.PhysicalFile;
import com.example.chatserver.entity.Space;
import com.example.chatserver.entity.SpaceFile;
import com.example.chatserver.entity.SpaceRecycle;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.PhysicalFileMapper;
import com.example.chatserver.mapper.SpaceFileMapper;
import com.example.chatserver.mapper.SpaceMapper;
import com.example.chatserver.mapper.SpaceRecycleMapper;
import com.example.chatserver.service.PhysicalFileService;
import com.example.chatserver.service.SpaceFileService;
import com.example.chatserver.service.SpaceService;
import com.example.chatserver.utils.ChunkUploadUtil;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.dto.CheckUploadResultDto;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    SpaceFileMapper spaceFileMapper;

    @Resource
    SpaceRecycleMapper spaceRecycleMapper;

    @Resource
    PhysicalFileMapper physicalFileMapper;

    @Resource
    SpaceFileService spaceFileService;

    @Resource
    PhysicalFileService physicalFileService;

    @Resource
    ChunkUploadUtil chunkUploadUtil;

    @Resource
    MinioUtil minioUtil;

    @Override
    public Space getOrCreateUserSpace(String userId) {
        Space space = getByUserId(userId);
        if (space != null) {
            return space;
        }

        Space newSpace = new Space();
        newSpace.setId(IdUtil.simpleUUID());
        newSpace.setUserId(userId);
        newSpace.setQuotaBytes(CloudDriveConstant.DEFAULT_USER_SPACE_QUOTA_BYTES);
        newSpace.setUsedBytes(0L);
        newSpace.setFileCount(0L);
        try {
            save(newSpace);
        } catch (DuplicateKeyException e) {
            //并发下已被其他请求创建，回查即可
        }

        space = getByUserId(userId);
        if (space == null) {
            throw new BaseException("云盘空间创建失败");
        }
        return space;
    }

    @Override
    public void checkSpaceQuota(Space space, long fileSize) {
        //quotaBytes为0表示不限制
        long quota = space.getQuotaBytes() == null ? 0 : space.getQuotaBytes();
        long used = space.getUsedBytes() == null ? 0 : space.getUsedBytes();
        if (quota > 0 && used + fileSize > quota) {
            throw new BaseException("云盘空间容量不足");
        }
    }

    @Override
    public List<SpaceFile> listUserFiles(String userId) {
        Space space = getOrCreateUserSpace(userId);
        LambdaQueryWrapper<SpaceFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceFile::getSpaceId, space.getId())
                .orderByDesc(SpaceFile::getUpdateTime);
        return spaceFileMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteUserFiles(String userId, List<String> spaceFileIds) {
        Space space = getOrCreateUserSpace(userId);
        List<String> uniqueIds = distinctIds(spaceFileIds);
        List<SpaceFile> files = spaceFileService.listByIds(uniqueIds);
        if (files.size() != uniqueIds.size()) {
            throw new BaseException("参数错误");
        }
        long totalSize = 0;
        for (SpaceFile file : files) {
            if (!file.getSpaceId().equals(space.getId())) {
                throw new BaseException("参数错误");
            }
            totalSize += file.getFileSize() == null ? 0 : file.getFileSize();
        }

        //软删除文件并写入回收站（默认保留30天），配额立即释放
        Date expireAt = DateUtil.offsetDay(new Date(), CloudDriveConstant.DEFAULT_RECYCLE_EXPIRE_DAYS);
        spaceFileService.removeByIds(uniqueIds);
        for (SpaceFile file : files) {
            SpaceRecycle recycle = new SpaceRecycle();
            recycle.setId(IdUtil.simpleUUID());
            recycle.setUserId(userId);
            recycle.setSpaceId(space.getId());
            recycle.setSpaceFileId(file.getId());
            recycle.setExpireAt(expireAt);
            spaceRecycleMapper.insert(recycle);
        }
        baseMapper.decUsedBytes(space.getId(), totalSize, files.size());
    }

    @Override
    public List<SpaceCategoryStatDto> listUserCategoryStats(String userId) {
        Space space = getOrCreateUserSpace(userId);
        List<SpaceCategoryStatDto> stats = spaceFileMapper.statByCategory(space.getId());
        Map<String, SpaceCategoryStatDto> statMap = stats.stream()
                .collect(Collectors.toMap(SpaceCategoryStatDto::getFileCategory, stat -> stat));

        //五类齐全返回，没有文件的分类补0
        List<String> categories = List.of(
                CloudDriveConstant.CATEGORY_IMAGE,
                CloudDriveConstant.CATEGORY_VIDEO,
                CloudDriveConstant.CATEGORY_DOCUMENT,
                CloudDriveConstant.CATEGORY_AUDIO,
                CloudDriveConstant.CATEGORY_ARCHIVE
        );
        List<SpaceCategoryStatDto> result = new ArrayList<>(categories.size());
        for (String category : categories) {
            SpaceCategoryStatDto stat = statMap.get(category);
            if (stat != null) {
                result.add(stat);
                continue;
            }
            result.add(new SpaceCategoryStatDto(category, 0L, 0L));
        }
        return result;
    }

    @Override
    public CheckUploadResultDto checkUpload(String userId, UploadCheckVo uploadCheckVo) {
        //类型白名单校验：不在图片/视频/音频/文档/压缩包后缀表内的文件不允许上传
        if (!CloudDriveConstant.isAllowedUploadExt(uploadCheckVo.getFileName())) {
            throw new BaseException("不支持的文件类型，仅支持图片/视频/音频/文档/压缩包");
        }
        Space space = getOrCreateUserSpace(userId);
        checkSpaceQuota(space, uploadCheckVo.getFileSize());

        //hash命中物理文件，直接建逻辑文件完成秒传
        PhysicalFile physicalFile = physicalFileService.getByHash(uploadCheckVo.getFileHash());
        if (physicalFile != null) {
            spaceFileService.createUserFileFromPhysicalFile(space, uploadCheckVo, physicalFile);
            return new CheckUploadResultDto(true, null);
        }
        //未命中返回已传分片，前端续传缺失分片
        return new CheckUploadResultDto(false, chunkUploadUtil.getUploadedChunks(uploadCheckVo.getFileHash()));
    }

    @Override
    public void uploadChunk(MultipartFile file, String fileHash, String chunkIndex) {
        try {
            chunkUploadUtil.uploadChunk(file, fileHash, chunkIndex);
        } catch (Exception e) {
            throw new BaseException("分片上传失败");
        }
    }

    @Override
    public String uploadMerge(String userId, UploadCheckVo uploadCheckVo) {
        if (!CloudDriveConstant.isAllowedUploadExt(uploadCheckVo.getFileName())) {
            throw new BaseException("不支持的文件类型，仅支持图片/视频/音频/文档/压缩包");
        }
        Space space = getOrCreateUserSpace(userId);
        checkSpaceQuota(space, uploadCheckVo.getFileSize());

        String objectName = chunkUploadUtil.mergeChunks(uploadCheckVo.getFileHash(),
                uploadCheckVo.getTotalChunk(), uploadCheckVo.getFileName());
        PhysicalFile physicalFile = physicalFileService.createFile(uploadCheckVo, objectName);
        spaceFileService.createUserFileFromPhysicalFile(space, uploadCheckVo, physicalFile);
        return objectName;
    }

    @Override
    public String getFileDownloadUrl(String userId, String spaceFileId) {
        Space space = getOrCreateUserSpace(userId);
        SpaceFile file = spaceFileService.getById(spaceFileId);
        if (file == null || !file.getSpaceId().equals(space.getId())) {
            throw new BaseException("参数错误");
        }
        //存储路径由物理文件表关联取，不再冗余存储在文件表
        PhysicalFile physicalFile = physicalFileService.getById(file.getPhysicalId());
        if (physicalFile == null) {
            throw new BaseException("参数错误");
        }
        String url = minioUtil.previewFile(physicalFile.getStoragePath());
        if (StringUtils.isBlank(url)) {
            throw new BaseException("生成下载链接失败");
        }
        return url;
    }

    private Space getByUserId(String userId) {
        LambdaQueryWrapper<Space> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Space::getUserId, userId);
        return getOne(queryWrapper);
    }

    //入参id去重，含空id直接报错
    private List<String> distinctIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BaseException("参数错误");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : ids) {
            if (StringUtils.isBlank(id)) {
                throw new BaseException("参数错误");
            }
            unique.add(id);
        }
        return new ArrayList<>(unique);
    }
}
