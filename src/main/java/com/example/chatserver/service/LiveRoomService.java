package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.LiveRoom;
import com.example.chatserver.dto.voip.LiveRoomInfoDto;
import com.example.chatserver.vo.live.UpdateLiveRoomTitleVo;
import java.io.InputStream;

public interface LiveRoomService extends IService<LiveRoom> {
    LiveRoom getOrCreate(String userId);

    LiveRoomInfoDto getInfo(String userId);

    boolean updateTitle(String userId, UpdateLiveRoomTitleVo vo);

    String uploadBackground(String userId, InputStream inputStream, String name, String type, long size);

}
