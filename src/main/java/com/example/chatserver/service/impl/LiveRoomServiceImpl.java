package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.LiveRoom;
import com.example.chatserver.entity.User;
import com.example.chatserver.dto.voip.LiveRoomInfoDto;
import com.example.chatserver.dto.voip.LiveRoomDto;
import com.example.chatserver.dto.voip.LiveDanmakuDto;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.LiveRoomMapper;
import com.example.chatserver.service.LiveKitTokenService;
import com.example.chatserver.service.LiveRoomService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.vo.live.PublishDanmakuVo;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import com.example.chatserver.utils.CallSessionUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LiveRoomServiceImpl extends ServiceImpl<LiveRoomMapper, LiveRoom> implements LiveRoomService {
    private static final String DEFAULT_TITLE = "这个人太懒，还没有给直播间起标题";
    private static final long DANMAKU_EXPIRE_SECONDS = 24 * 60 * 60;
    private static final String DANMAKU_TOPIC = "danmaku";

    @Resource
    UserService userService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    LiveRoomMapper liveRoomMapper;

    @Resource
    RedisUtils redisUtils;

    @Resource
    LiveKitTokenService liveKitTokenService;

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
    public List<LiveRoomDto> getLiveRooms(List<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return List.of();

        List<String> userIds = sessionIds.stream()
                .map(CallSessionUtil::parseLiveUserId)
                .distinct()
                .toList();
        Map<String, LiveRoomInfoDto> roomMap = liveRoomMapper.selectInfoByUserIds(userIds).stream()
                .collect(Collectors.toMap(LiveRoomInfoDto::getUserId, Function.identity()));

        return sessionIds.stream().map(sessionId -> {
            String userId = CallSessionUtil.parseLiveUserId(sessionId);
            LiveRoomInfoDto info = roomMap.get(userId);
            if (info == null) return null;

            LiveRoomDto result = new LiveRoomDto();
            result.setSessionId(sessionId);
            result.setUserId(userId);
            result.setTitle(info.getTitle());
            result.setBackground(info.getBackground());
            result.setPortrait(info.getPortrait());
            return result;
        }).filter(java.util.Objects::nonNull).toList();
    }

    @Override
    public List<LiveDanmakuDto> getDanmakuList(String sessionId) {
        CallSessionUtil.parseLiveUserId(sessionId);

        List<Object> values = redisUtils.lGet(sessionId, 0, -1);
        if (values == null || values.isEmpty()) return List.of();

        return values.stream()
                .map(this::toDanmaku)
                .toList();
    }

    @Override
    public LiveDanmakuDto publishDanmaku(String userId, PublishDanmakuVo vo) {
        String sessionId = vo.getSessionId().trim();
        CallSessionUtil.parseLiveUserId(sessionId);

        if (userId == null || userId.isBlank()) {
            throw new BaseException("用户不存在");
        }

        // 用户信息使用 UserService 中约定的缓存 key，并在用户资料更新时由 UserService 负责删除。
        String userKey = "user:" + userId;
        String userJson = (String) redisUtils.get(userKey);
        User user;
        if (userJson != null) {
            user = JSONUtil.toBean(userJson, User.class);
        } else {
            user = userService.getById(userId);
            if (user == null) throw new BaseException("用户不存在");
            redisUtils.set(userKey, JSONUtil.toJsonStr(user), 30 * 60);
        }

        LiveDanmakuDto danmaku = new LiveDanmakuDto();
        danmaku.setSessionId(sessionId);
        danmaku.setUserId(userId);
        danmaku.setUsername(user.getName());
        danmaku.setContent(vo.getContent().trim());
        danmaku.setCreateTime(LocalDateTime.now());

        String data = JSONUtil.toJsonStr(danmaku);
        if (!redisUtils.lSet(sessionId, data, DANMAKU_EXPIRE_SECONDS)) {
            throw new BaseException("弹幕保存失败");
        }

        liveKitTokenService.sendData(sessionId, data, DANMAKU_TOPIC);
        return danmaku;
    }

    private LiveDanmakuDto toDanmaku(Object value) {
        if (value instanceof LiveDanmakuDto danmaku) return danmaku;
        if (value instanceof String json) return JSONUtil.toBean(json, LiveDanmakuDto.class);
        return JSONUtil.toBean(String.valueOf(value), LiveDanmakuDto.class);
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
