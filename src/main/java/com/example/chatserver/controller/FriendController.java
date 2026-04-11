package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.friend.*;
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
     * 获取好友列表
     */
    @GetMapping("/list/flat")
    public JSONObject getFriendListFlat(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlat(userId, friendInfo);
        return ResultUtil.Succeed(friendListDto);
    }

    /**
     * 判断是否是好友
     */
    @GetMapping("/is/friend")
    public JSONObject isFriend(@Userid String userId, @RequestParam String targetId) {
        boolean result = friendService.isFriendIgnoreSpecial(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取好友列表(未读消息数)
     */
    @GetMapping("/list/flat/unread")
    public JSONObject getFriendListFlatUnread(@Userid String userId, @RequestParam(defaultValue = "") String friendInfo) {
        List<Friend> friendListDto = friendService.getFriendListFlatUnread(userId, friendInfo);
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

    /**
     * 同意好友请求(好友申请接口在notify)
     */
    @PostMapping("/agree")
    public JSONObject agreeFriendApply(@Userid String userId, @RequestBody AgreeFriendApplyVo agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 同意好友请求(批量)
     */
    @PostMapping("/agree/id")
    public JSONObject agreeFriendApplyFromId(@Userid String userId, @RequestBody AgreeFriendApplyVo agreeFriendApplyVo) {
        boolean result = friendService.agreeFriendApply(userId, agreeFriendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }

    /**
     * 拒绝好友请求
     */
    @PostMapping("/reject")
    public JSONObject refuseFriendApply(@Userid String userId, @RequestBody RejectFriendApplyVo friendApplyVo) {
        boolean result = friendService.rejectFriendApply(userId, friendApplyVo.getFromId());
        return ResultUtil.Succeed(result);
    }

    /**
     * 扫码好友请求（立即建立好友关系）
     */
    @PostMapping("/add/qr")
    public JSONObject addFriendByQr(@Userid String userId, @RequestBody AddFriendByQrVo AddFriendByQrVo) {
        String targetId = SecurityUtil.aesDecrypt(AddFriendByQrVo.getQrCode()); //解密出好友id
        boolean result = friendService.addFriendApply(userId, targetId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友备注
     */
    @PostMapping("/set/remark")
    public JSONObject setRemark(@Userid String userId, @RequestBody SetRemarkVo setRemarkVo) {
        boolean result = friendService.setRemark(userId, setRemarkVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置好友分组
     */
    @PostMapping("/set/group")
    public JSONObject setGroup(@Userid String userId, @RequestBody SetGroupVo setGroupVo) {
        boolean result = friendService.setGroup(userId, setGroupVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    public JSONObject deleteFriend(@Userid String userId, @RequestBody DeleteFriendVo deleteFriendVo) {
        boolean result = friendService.deleteFriend(userId, deleteFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 特别关心
     */
    @PostMapping("/carefor")
    public JSONObject careForFriend(@Userid String userId, @RequestBody CareForFriendVo careForFriendVo) {
        boolean result = friendService.careForFriend(userId, careForFriendVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 特别关心
     */
    @PostMapping("/uncarefor")
    public JSONObject unCareForFriend(@Userid String userId, @RequestBody UnCareForFriendVo unCareForFriendVo) {
        boolean result = friendService.unCareForFriend(userId, unCareForFriendVo);
        return ResultUtil.ResultByFlag(result);
    }
}

