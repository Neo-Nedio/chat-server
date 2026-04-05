package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.FriendDetailsDto;
import com.example.chatserver.dto.FriendListDto;
import com.example.chatserver.entity.Friend;
import com.example.chatserver.vo.friend.AgreeFriendApplyVo;
import com.example.chatserver.vo.friend.SearchFriendsVo;
import com.example.chatserver.vo.friend.SetRemarkVo;

import java.util.List;


public interface FriendService extends IService<Friend> {
    List<FriendListDto> getFriendList(String userId);

    boolean isFriend(String userId, String friendId);

    FriendDetailsDto getFriendDetails(String userId, String friendId);

    List<FriendDetailsDto> searchFriends(String userId, SearchFriendsVo searchFriendsVo);

    boolean agreeFriendApply(String userId, AgreeFriendApplyVo agreeFriendApplyVo);

    boolean updateGroupId(String userId, String oldGroupId, String newGroupId);

    boolean setRemark(String userId, SetRemarkVo setRemarkVo);
}
