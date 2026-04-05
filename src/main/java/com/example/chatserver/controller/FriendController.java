package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.friend.SearchFriendsVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/api/friend")
public class FriendController {

    @Resource
    FriendService friendService;

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public JSONObject getFriendList(@Userid String userId) {
        List<FriendListDto> friendListDto = friendService.getFriendList(userId);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 获取好友详情
     */
    @GetMapping("/details/{friendId}")
    public JSONObject getFriendDetails(@Userid String userId, @PathVariable String friendId) {
        FriendDetailsDto friendDetailsDto = friendService.getFriendDetails(userId, friendId);
        return ResultUtil.Succeed(friendDetailsDto);
    }

    /**
     * 搜索好友
     */
    @PostMapping("/search")
    public JSONObject searchFriends(@Userid String userId, @RequestBody SearchFriendsVo searchFriendsVo) {
        List<FriendDetailsDto> result = friendService.searchFriends(userId, searchFriendsVo);
        return ResultUtil.Succeed(result);
    }
}

