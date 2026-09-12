package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.LiveRoom;
import com.example.chatserver.entity.User;
import com.example.chatserver.dto.voip.LiveRoomInfoDto;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.LiveRoomMapper;
import com.example.chatserver.service.LiveRoomService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;

@Service
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {
    private static final String DEFAULT_TITLE = "这个人太懒，还没有给直播间起标题";

    @Resource
    UserService userService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    LiveRoomMapper liveRoomMapper;

    @Override
    public LiveRoom getOrCreate(String userId) {
        LiveRoom liveRoom = getOne(new LambdaQueryWrapper<LiveRoom>().eq(LiveRoom::getUserId, userId));
        if (liveRoom != null) return liveRoom;

        User user = userService.getById(userId);
        if (user == null) throw new BaseException("用户不存在");

        liveRoom = new LiveRoom();
        liveRoom.setId(IdUtil.randomUUID());
        liveRoom.setUserId(userId);
        liveRoom.setTitle(DEFAULT_TITLE);
        save(liveRoom);
        return liveRoom;
    }

    @Override
    public LiveRoomInfoDto getInfo(String userId) {
        getOrCreate(userId);
        return liveRoomMapper.selectInfo(userId);
    }

    @Override
    public boolean updateTitle(String userId, UpdateLiveRoomTitleVo vo) {
        LiveRoom liveRoom = getOrCreate(userId);
        liveRoom.setTitle(vo.getTitle().trim());
        return updateById(liveRoom);
    }

    @Override
    public String uploadBackground(String userId, InputStream inputStream, String name, String type, long size) {
        LiveRoom liveRoom = getOrCreate(userId);
        String oldFile = liveRoom.getBackground();
        if (oldFile != null && !oldFile.isBlank()) minioUtil.remove(oldFile);

        String suffix = name.substring(name.lastIndexOf('.'));
        String fileName = userId + "-live-background-" + System.currentTimeMillis() + suffix;
        String url = minioUtil.upload(inputStream, fileName, type, size);
        if (url == null) throw new BaseException("图片上传失败");
        liveRoom.setBackground(fileName);
        updateById(liveRoom);
        return url;
    }
}
