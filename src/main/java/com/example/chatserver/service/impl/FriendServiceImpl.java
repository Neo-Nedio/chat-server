package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.FriendApplyStatus;
import com.example.chatserver.constant.NotifyType;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.entity.Group;
import com.example.chatserver.entity.Notify;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.FriendMapper;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.GroupService;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.vo.friend.AgreeFriendApplyVo;
import com.example.chatserver.vo.friend.SearchFriendsVo;
import com.example.chatserver.websocket.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    GroupService groupService;

    @Resource
    FriendMapper friendMapper;

    @Resource
    NotifyService notifyService;

    @Resource
    WebSocketService webSocketService;


    @Override
    //获得好友所有分组和相应分组下的好友
    public List<FriendListDto> getFriendList(String userId) {
        List<FriendListDto> friendListDtoS = new ArrayList<>();
        //将没有分组的好像添加到未分组中
        List<Friend> ungroupFriends = friendMapper.getFriendByUserIdAndGroupId(userId, "0");
        if (null != ungroupFriends && !ungroupFriends.isEmpty()) {
            FriendListDto ungroupFriendListDto = new FriendListDto();
            ungroupFriendListDto.setName("未分组");
            ungroupFriendListDto.setFriends(ungroupFriends);
            friendListDtoS.add(ungroupFriendListDto);
        }
        //查询用户当前分组
        List<Group> groups = groupService.getGroupByUserId(userId);
        //遍历分组，查询分组下的好友
        groups.forEach((group) -> {
            List<Friend> friends = friendMapper.getFriendByUserIdAndGroupId(userId, group.getId());
            FriendListDto friendListDto = new FriendListDto();
            friendListDto.setGroupId(group.getId());
            friendListDto.setName(group.getName());
            friendListDto.setFriends(friends);
            friendListDtoS.add(friendListDto);
        });
        return friendListDtoS;
    }

    @Override
    public boolean isFriend(String userId, String friendId) {
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        return count(queryWrapper) > 0;
    }

    @Override
    public FriendDetailsDto getFriendDetails(String userId, String friendId) {
        boolean isFriend = isFriend(userId, friendId);
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        return friendMapper.getFriendDetails(userId, friendId);
    }

    @Override
    public List<FriendDetailsDto> searchFriends(String userId, SearchFriendsVo searchFriendsVo) {
        return friendMapper.searchFriends(userId, "%" + searchFriendsVo.getFriendInfo() + "%");
    }

    /**
     * 添加好友
     */
    public boolean addFriend(String userId, String targetId) {
        //判断目标是否是自己好友
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, targetId);
        if (count(queryWrapper) <= 0) {
            Friend friend = new Friend();
            friend.setId(IdUtil.randomUUID());
            friend.setUserId(userId);
            friend.setFriendId(targetId);
            return save(friend);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean agreeFriendApply(String userId, AgreeFriendApplyVo agreeFriendApplyVo) {
        //判断申请存在并且是对面发起
        Notify notify = notifyService.getById(agreeFriendApplyVo.getNotifyId());
        if (null == notify
                || !notify.getToId().equals(userId)
                || !notify.getType().equals(NotifyType.Friend_Apply)
                || !notify.getStatus().equals(FriendApplyStatus.Wait)
        ) {
            throw new BaseException("没有添加好友申请");
        }
        //双方添加好友
        addFriend(userId, notify.getFromId());
        addFriend(notify.getFromId(), userId);
        //更新通知
        notify.setStatus(FriendApplyStatus.Agree);
        notify.setUnreadId(notify.getFromId());
        notifyService.updateById(notify);
        //发送通知
        webSocketService.sendNotifyToUser(notify, notify.getFromId());
        return true;
    }
}
