package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.admin.vo.notify.DeleteNotifyVo;
import com.example.chatserver.dto.FriendNotifyDto;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.entity.Notify;
import com.example.chatserver.vo.notify.FriendApplyNotifyVo;
import com.example.chatserver.vo.notify.ReadNotifyVo;


import java.util.List;


public interface NotifyService extends IService<Notify> {

    boolean friendApplyNotify(String userId,String userRole, FriendApplyNotifyVo friendApplyNotifyVo);

    List<FriendNotifyDto> friendListNotify(String userId);

    int unread(String userId);

    int unreadByType(String userId, String type);

    boolean readNotify(String userId, ReadNotifyVo readNotifyVo);

    List<SystemNotifyDto> SystemListNotify(String userId);

    boolean deleteNotify(DeleteNotifyVo deleteNotifyVo);

    boolean createNotify(String url, String title, String text);
}
