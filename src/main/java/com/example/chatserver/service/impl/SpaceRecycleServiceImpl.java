package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.Space;
import com.example.chatserver.entity.SpaceFile;
import com.example.chatserver.entity.SpaceRecycle;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.PhysicalFileMapper;
import com.example.chatserver.mapper.SpaceFileMapper;
import com.example.chatserver.mapper.SpaceMapper;
import com.example.chatserver.mapper.SpaceRecycleMapper;
import com.example.chatserver.service.SpaceRecycleService;
import com.example.chatserver.service.SpaceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class SpaceRecycleServiceImpl extends ServiceImpl<SpaceRecycleMapper, SpaceRecycle> implements SpaceRecycleService {

    @Resource
    SpaceMapper spaceMapper;

    @Resource
    SpaceFileMapper spaceFileMapper;

    @Resource
    PhysicalFileMapper physicalFileMapper;

    @Resource
    SpaceService spaceService;

    //自注入代理，保证cleanExpiredRecycles入口调用本类事务方法时事务生效
    @Resource
    @Lazy
    SpaceRecycleService selfService;

    @Override
    public List<SpaceRecycle> listUserRecycle(String userId) {
        Space space = spaceService.getOrCreateUserSpace(userId);
        return baseMapper.listByUserIdAndSpaceId(userId, space.getId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void restoreUserRecycle(String userId, List<String> spaceRecycleIds) {
        Space space = spaceService.getOrCreateUserSpace(userId);
        List<SpaceRecycle> recycles = baseMapper.listValidByIds(spaceRecycleIds, userId, space.getId());
        if (recycles == null || recycles.isEmpty()) {
            throw new BaseException("参数错误");
        }
        List<String> spaceFileIds = recycles.stream().map(SpaceRecycle::getSpaceFileId).collect(Collectors.toList());
        List<String> recycleIds = recycles.stream().map(SpaceRecycle::getId).collect(Collectors.toList());

        //还原需要读取回收站中的软删除记录
        List<SpaceFile> files = spaceFileMapper.listByIdsAll(spaceFileIds);
        if (files.size() != spaceFileIds.size()) {
            throw new BaseException("参数错误");
        }
        long totalSize = 0;
        for (SpaceFile file : files) {
            if (!file.getSpaceId().equals(space.getId())) {
                throw new BaseException("参数错误");
            }
            totalSize += file.getFileSize() == null ? 0 : file.getFileSize();
        }
        //还原前校验容量是否够用
        spaceService.checkSpaceQuota(space, totalSize);

        spaceFileMapper.restoreByIds(spaceFileIds); //spaceFile还原
        baseMapper.permanentDeleteByIds(recycleIds); //永久从回收站删除记录
        spaceMapper.incUsedBytes(space.getId(), totalSize, files.size());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void permanentlyDeleteUserRecycle(String userId, List<String> spaceRecycleIds) {
        Space space = spaceService.getOrCreateUserSpace(userId);
        List<SpaceRecycle> recycles = baseMapper.listByIdsIgnoreExpire(spaceRecycleIds, userId, space.getId());
        if (recycles == null || recycles.isEmpty()) {
            throw new BaseException("参数错误");
        }
        doPermanentDelete(space, recycles);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void clearUserRecycle(String userId) {
        Space space = spaceService.getOrCreateUserSpace(userId);
        List<String> ids = baseMapper.listIdsByUserIdAndSpaceId(userId, space.getId());
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SpaceRecycle> recycles = baseMapper.listByIdsIgnoreExpire(ids, userId, space.getId());
        if (recycles == null || recycles.isEmpty()) {
            return;
        }
        doPermanentDelete(space, recycles);
    }

    @Override
    public void cleanExpiredRecycles() {
        List<SpaceRecycle> expired = baseMapper.listExpired();
        if (expired == null || expired.isEmpty()) {
            return;
        }
        //按用户分组后走彻底删除流程，单用户失败不影响其他用户
        Map<String, List<String>> userRecycleMap = expired.stream()
                .collect(Collectors.groupingBy(SpaceRecycle::getUserId,
                        Collectors.mapping(SpaceRecycle::getId, Collectors.toList())));
        for (Map.Entry<String, List<String>> entry : userRecycleMap.entrySet()) {
            try {
                selfService.permanentlyDeleteUserRecycle(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("清理过期回收站失败, userId: {}", entry.getKey(), e);
            }
        }
    }

    //彻底删除：物理文件引用计数减1 + 文件/记录物理删除（配额在删除入站时已扣）；
    //归零的物理文件由每日定时任务统一清理记录与MinIO对象（见PhysicalFileCleanTask）
    private void doPermanentDelete(Space space, List<SpaceRecycle> recycles) {
        List<String> spaceFileIds = recycles.stream().map(SpaceRecycle::getSpaceFileId).collect(Collectors.toList());
        List<String> recycleIds = recycles.stream().map(SpaceRecycle::getId).collect(Collectors.toList());

        List<SpaceFile> files = spaceFileMapper.listByIdsAll(spaceFileIds);
        if (files.size() != spaceFileIds.size()) {
            throw new BaseException("参数错误");
        }
        for (SpaceFile file : files) {
            if (!file.getSpaceId().equals(space.getId())) {
                throw new BaseException("参数错误");
            }
            //物理文件引用计数减1
            if (StringUtils.isNotBlank(file.getPhysicalId())) {
                physicalFileMapper.decRefCount(file.getPhysicalId());
            }
        }
        spaceFileMapper.permanentDeleteByIds(spaceFileIds);
        baseMapper.permanentDeleteByIds(recycleIds);
    }
}
