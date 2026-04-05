package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.entity.Group;
import com.example.chatserver.mapper.FriendMapper;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.GroupService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Resource
    GroupService groupService;

    @Resource
    FriendMapper friendMapper;


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
}
