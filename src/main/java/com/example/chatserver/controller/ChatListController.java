package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.FriendList;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.utils.ResultUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/v1/api/chat-list")
public class ChatListController {
    @Resource
    FriendService friendService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/list")
    public JSONObject getChatList(@Userid String userId) {
        List<FriendList> friendList = friendService.getFriendList(userId);
        return ResultUtil.Succeed(friendList);
    }
}

