package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * " +
            "FROM (SELECT * " +
            "      FROM `message` " +
            "      WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "         OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "      ORDER BY `create_time` DESC LIMIT #{index}, #{num}) AS subquery " +
            "ORDER BY `create_time` ASC")
    //内层倒序取最新N条，外层正序排列，这样既拿到了最新的消息，展示顺序又是正确的（早在上，晚在下）。
    //用于正常聊天显示，倒叙获取需要的数量，再正序回来
    @ResultMap("mybatis-plus_Message")
    List<Message> messageRecord(@Param("userId") String userId,
                                @Param("targetId") String targetId,
                                @Param("index") int index,
                                @Param("num") int num);

    @Select("SELECT * " +
            "FROM (SELECT * " +
            "      FROM `message` " +
            "      WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "         OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "      ORDER BY `create_time` DESC LIMIT #{index}, #{num}) AS subquery ")
    @ResultMap("mybatis-plus_Message")
    //用于加载更多，倒叙获取对应数量的聊天消息
    List<Message> messageRecordDesc(@Param("userId") String userId, @Param("targetId") String targetId,
                                    @Param("index") int index, @Param("num") int num);


    @Select("SELECT * " +
            "FROM `message` " +
            "WHERE (`from_id` = #{userId} AND `to_id` = #{targetId}) " +
            "   OR (`from_id` = #{targetId} AND `to_id` = #{userId}) " +
            "ORDER BY `create_time` DESC LIMIT 1")
    @ResultMap("mybatis-plus_Message")
    Message getPreviousShowTimeMsg(@Param("userId") String userId, @Param("targetId") String toUserId);

}
