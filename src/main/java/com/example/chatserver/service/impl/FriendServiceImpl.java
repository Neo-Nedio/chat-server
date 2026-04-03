package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.FriendList;
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
    public List<FriendList> getFriendList(String userId) {
        List<FriendList> friendLists = new ArrayList<>();
        //将没有分组的好像添加到未分组中
        List<Friend> ungroupFriends = friendMapper.getFriendByUserIdAndGroupId(userId, "0");
        if (null != ungroupFriends && !ungroupFriends.isEmpty()) {
            FriendList ungroupFriendList = new FriendList();
            ungroupFriendList.setName("未分组");
            ungroupFriendList.setFriends(ungroupFriends);
            friendLists.add(ungroupFriendList);
        }
        //查询用户当前分组
        List<Group> groups = groupService.getGroupByUserId(userId);
        //遍历分组，查询分组下的好友
        groups.forEach((group) -> {
            List<Friend> friends = friendMapper.getFriendByUserIdAndGroupId(userId, group.getId());
            FriendList friendList = new FriendList();
            friendList.setGroupId(group.getId());
            friendList.setName(group.getName());
            friendList.setFriends(friends);
            friendLists.add(friendList);
        });
        return friendLists;
    }
}
