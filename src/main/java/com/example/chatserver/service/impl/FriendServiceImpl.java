package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.FriendApplyStatus;
import com.example.chatserver.constant.NotifyType;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.dto.TalkContentDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.entity.Group;
import com.example.chatserver.entity.Notify;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.FriendMapper;
import com.example.chatserver.service.*;
import com.example.chatserver.vo.friend.*;
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
    UserService userService;

    @Resource
    TalkService talkService;

    @Resource
    ChatListService chatListService;

    @Resource
    WebSocketService webSocketService;


    @Override
    //获得好友所有分组和相应分组下的好友
    public List<FriendListDto> getFriendList(String userId) {
        List<FriendListDto> friendListDtoS = new ArrayList<>();
        //特别关心
        List<Friend> concernFriends = friendMapper.getConcernFriendByUser(userId);
        if (null != concernFriends && !concernFriends.isEmpty()) {
            FriendListDto concernFriendListDto = new FriendListDto();
            concernFriendListDto.setName("特别关心");
            concernFriendListDto.setFriends(concernFriends);
            concernFriendListDto.setCustom(false);
            friendListDtoS.add(concernFriendListDto);
        }
        //将没有分组的好像添加到未分组中
        List<Friend> ungroupFriends = friendMapper.getFriendByUserIdAndGroupId(userId, "0");
        if (null != ungroupFriends && !ungroupFriends.isEmpty()) {
            FriendListDto ungroupFriendListDto = new FriendListDto();
            ungroupFriendListDto.setName("未分组");
            ungroupFriendListDto.setFriends(ungroupFriends);
            ungroupFriendListDto.setCustom(false);
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
            friendListDto.setCustom(true);
            friendListDtoS.add(friendListDto);
        });
        return friendListDtoS;
    }

    /**
     * 判断是否是好友（忽略管理员，三方用户)
     */
    @Override
    public boolean isFriendIgnoreSpecial(String userId, String friendId) {
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        return count(queryWrapper) > 0;
    }

    @Override
    public boolean isFriend(String userId, String friendId) {
        //如果是管理员，当作双方是好友
        User friend = userService.getById(friendId);
        User user = userService.getById(userId);
        if (null != user && (UserRole.Admin.equals(user.getRole()) || UserRole.Admin.equals(friend.getRole()))) {
            return true;
        }
        if (user != null && UserRole.Third.equals(user.getRole())) {
            return true;
        }

        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        return count(queryWrapper) > 0;
    }

    @Override
    public FriendDetailsDto getFriendDetails(String userId, String friendId) {
        boolean isFriend = isFriendIgnoreSpecial(userId, friendId);
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        FriendDetailsDto friendDetailsDto = friendMapper.getFriendDetails(userId, friendId);
        TalkContentDto talkContentDto = talkService.getFriendLatestTalkContent(userId, friendId);
        friendDetailsDto.setTalkContent(talkContentDto);
        return friendDetailsDto;
    }

    @Override
    public List<FriendDetailsDto> searchFriends(String userId, SearchFriendsVo searchFriendsVo) {
        return friendMapper.searchFriends(userId, "%" + searchFriendsVo.getFriendInfo() + "%");
    }

    /**
     * 添加好友
     */
    public boolean addFriend(String userId, String targetId) {
        User user = userService.getById(userId);
        if (null == user) {
            throw new BaseException("用户不存在");
        }
        user = userService.getById(targetId);
        if (null == user) {
            throw new BaseException("用户不存在");
        }
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
        return true;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    //好友申请后的添加好友
    public boolean addFriendApply(String userId, String targetId) {
        //双方添加好友
        addFriend(userId, targetId);
        return addFriend(targetId, userId);
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
        boolean result = addFriendApply(userId, notify.getFromId());
        //更新通知
        notify.setStatus(FriendApplyStatus.Agree);
        notify.setUnreadId(notify.getFromId());
        notifyService.updateById(notify);
        //发送通知
        webSocketService.sendNotifyToUser(notify, notify.getFromId());
        return result;
    }

    @Override
    public boolean updateGroupId(String userId, String oldGroupId, String newGroupId) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, newGroupId)
                .eq(Friend::getGroupId, oldGroupId)
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean setRemark(String userId, SetRemarkVo setRemarkVo) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getRemark, setRemarkVo.getRemark())
                .eq(Friend::getFriendId, setRemarkVo.getFriendId())
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean setGroup(String userId, SetGroupVo setGroupVo) {
        boolean isExist = groupService.IsExistGroupByUserId(userId, setGroupVo.getGroupId());
        if (!isExist) {
            throw new BaseException("分组不存在");
        }
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getGroupId, setGroupVo.getGroupId())
                .eq(Friend::getFriendId, setGroupVo.getFriendId())
                .eq(Friend::getUserId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean deleteFriend(String userId, DeleteFriendVo deleteFriendVo) {
        //同时删除两个方向的好友记录
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        // 条件1：userId 是当前用户，friendId 是要删除的好友
        queryWrapper.or((q) -> {
                    q.eq(Friend::getUserId, userId)
                            .eq(Friend::getFriendId, deleteFriendVo.getFriendId());
                })
                // 条件2：userId 是要删除的好友，friendId 是当前用户
                .or((q) -> {
                    q.eq(Friend::getFriendId, userId)
                            .eq(Friend::getUserId, deleteFriendVo.getFriendId());
                });
        chatListService.removeByUserId(userId, deleteFriendVo.getFriendId());
        return remove(queryWrapper);
    }

    @Override
    public boolean careForFriend(String userId, CareForFriendVo careForFriendVo) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getIsConcern, true)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, careForFriendVo.getFriendId());
        return update(updateWrapper);
    }

    @Override
    public boolean unCareForFriend(String userId, UnCareForFriendVo unCareForFriendVo) {
        LambdaUpdateWrapper<Friend> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Friend::getIsConcern, false)
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, unCareForFriendVo.getFriendId());
        return update(updateWrapper);
    }

    @Override
    public List<Friend> getFriendListFlat(String userId, String friendInfo) {
        return friendMapper.getFriendListFlat(userId, friendInfo);
    }

    @Override
    public List<Friend> getFriendListFlatUnread(String userId, String friendInfo) {
        return friendMapper.getFriendListFlatUnread(userId, friendInfo);
    }
}
