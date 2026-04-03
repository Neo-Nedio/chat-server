package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.Friend;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface FriendMapper extends BaseMapper<Friend> {

    @Select("SELECT f.*, u.`name` AS `name` FROM `friend` AS f " +
            "JOIN `user` AS u ON f.`friend_id` = u.`id` " +
            "WHERE f.`user_id` = #{userId} AND f.`group_id`= #{groupId}")
    //先从好友表找到对应好友id，再去用户表找对应名字
    List<Friend> getFriendByUserIdAndGroupId(@Param("userId") String userId, @Param("groupId") String groupId);
}
