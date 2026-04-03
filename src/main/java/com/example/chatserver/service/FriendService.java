package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.FriendList;
import com.example.chatserver.entity.Friend;

import java.util.List;


public interface FriendService extends IService<Friend> {
    List<FriendList> getFriendList(String userId);
}
