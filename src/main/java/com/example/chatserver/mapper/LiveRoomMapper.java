package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.LiveRoom;
import com.example.chatserver.dto.voip.LiveRoomInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LiveRoomMapper extends BaseMapper<LiveRoom> {
    @Select("SELECT lr.id, lr.user_id, lr.title, lr.background, u.portrait, " +
            "lr.create_time, lr.update_time FROM live_room lr " +
            "INNER JOIN `user` u ON u.id = lr.user_id WHERE lr.user_id = #{userId}")
    LiveRoomInfoDto selectInfo(String userId);

    @Select("<script>" +
            "SELECT lr.id, lr.user_id, lr.title, lr.background, u.portrait, " +
            "lr.create_time, lr.update_time FROM live_room lr " +
            "INNER JOIN `user` u ON u.id = lr.user_id " +
            "WHERE lr.user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    List<LiveRoomInfoDto> selectInfoByUserIds(@Param("userIds") List<String> userIds);
}
