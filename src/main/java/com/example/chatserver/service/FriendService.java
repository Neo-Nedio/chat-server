package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.vo.friend.*;

import java.util.List;


public interface FriendService extends IService<Friend> {
    List<FriendListDto> getFriendList(String userId);

    boolean isFriendIgnoreSpecial(String userId, String friendId);

    boolean isFriend(String userId, String friendId);

    FriendDetailsDto getFriendDetails(String userId, String friendId);

    List<FriendDetailsDto> searchFriends(String userId, SearchVo searchFriendsVo);

    boolean agreeFriendApply(String userId, AgreeFriendApplyVo agreeFriendApplyVo);

    boolean agreeFriendApply(String userId,String fromId);

    boolean addFriendApply(String userId, String targetId);

    boolean rejectFriendApply(String userId, String fromId);

    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

    boolean setRemark(String userId, SetRemarkVo setRemarkVo);

    boolean setGroup(String userId, SetGroupVo setGroupVo);

    boolean deleteFriend(String userId, DeleteFriendVo deleteFriendVo);

    boolean careForFriend(String userId, CareForFriendVo careForFriendVo);

    boolean unCareForFriend(String userId, UnCareForFriendVo unCareForFriendVo);

    List<Friend> getFriendListFlat(String userId, String friendInfo);

    List<Friend> getFriendListFlatUnread(String userId, String friendInfo);
}
