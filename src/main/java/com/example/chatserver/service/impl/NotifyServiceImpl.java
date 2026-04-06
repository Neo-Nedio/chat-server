package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.FriendApplyStatus;
import com.example.chatserver.constant.NotifyType;
import com.example.chatserver.dto.FriendNotifyDto;
import com.example.chatserver.entity.Notify;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.NotifyMapper;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.vo.notify.FriendApplyNotifyVo;
import com.example.chatserver.vo.notify.ReadNotifyVo;
import com.example.chatserver.websocket.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notify> implements NotifyService {

    @Lazy
    @Resource
    FriendService friendService;

    @Resource
    NotifyMapper notifyMapper;

    @Resource
    WebSocketService webSocketService;

    @Override
    //发送好友申请
    public boolean friendApplyNotify(String userId, FriendApplyNotifyVo friendApplyNotifyVo) {
        boolean isFriend = friendService.isFriend(userId, friendApplyNotifyVo.getUserId());
        if (isFriend) {
            throw new BaseException("ta已是您的好友");
        }

        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setFromId(userId);
        notify.setToId(friendApplyNotifyVo.getUserId());
        notify.setType(NotifyType.Friend_Apply);
        notify.setStatus(FriendApplyStatus.Wait);
        notify.setContent(friendApplyNotifyVo.getContent());
        notify.setUnreadId(friendApplyNotifyVo.getUserId());
        webSocketService.sendNotifyToUser(notify, friendApplyNotifyVo.getUserId());
        return save(notify);
    }

    @Override
    //好友申请列表
    public List<FriendNotifyDto> friendListNotify(String userId) {
        return notifyMapper.friendListNotify(userId, NotifyType.Friend_Apply);
    }

    @Override
    //未读通知数量
    public int unread(String userId) {
        Integer num = notifyMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

    @Override
    //通知已读
    public boolean readNotify(String userId, ReadNotifyVo readNotifyVo) {
        LambdaUpdateWrapper<Notify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Notify::getUnreadId, "")
                .eq(Notify::getUnreadId, userId)
                .eq(Notify::getType, readNotifyVo.getNotifyType());
        return update(updateWrapper);
    }
}
