package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.LiveRoom;
import com.example.chatserver.dto.voip.LiveRoomInfoDto;
import com.example.chatserver.dto.voip.LiveRoomDto;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import com.example.chatserver.vo.live.PublishDanmakuVo;
import com.example.chatserver.dto.voip.LiveDanmakuDto;
import java.io.InputStream;
import java.util.List;

public interface LiveRoomService extends IService<LiveRoom> {
    LiveRoom getOrCreate(String userId);

    LiveRoomInfoDto getInfo(String userId);

    List<LiveRoomDto> getLiveRooms(List<String> sessionIds);

    List<LiveDanmakuDto> getDanmakuList(String sessionId);

    LiveDanmakuDto publishDanmaku(String userId, PublishDanmakuVo vo);

    boolean updateTitle(String userId, UpdateLiveRoomTitleVo vo);

    String uploadBackground(String userId, InputStream inputStream, String name, String type, long size);

}
