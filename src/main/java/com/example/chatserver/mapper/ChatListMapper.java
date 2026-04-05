package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.ChatList;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface ChatListMapper extends BaseMapper<ChatList> {

    @Select("SELECT c.*, u.`name` AS `name`, f.`remark` " +
            "FROM `chat_list` AS c " +
            "JOIN `user` AS u ON c.`from_id` = u.`id` " +
            "JOIN `friend` AS f ON c.`from_id` = f.`friend_id` AND c.`user_id` = f.`user_id` " +
            "WHERE c.`user_id` = #{userId} AND c.`is_top` = #{isTop} " +
            "ORDER BY c.`update_time` DESC")
    @ResultMap("mybatis-plus_ChatList")
    List<ChatList> getChatListByUserIdAndIsTop(@Param("userId") String userId, @Param("isTop") boolean isTop);
}
