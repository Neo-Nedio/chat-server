package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper extends BaseMapper<User> {

    @Select("select * from `user` where `id` = #{userId}")
    UserDto info(String userId);

    @Select("SELECT * FROM user " +
            "WHERE account LIKE CONCAT('%', #{userInfo}, '%') " +
            "   OR phone LIKE CONCAT('%', #{userInfo}, '%') " +
            "   OR email LIKE CONCAT('%', #{userInfo}, '%') " +
            "   OR name LIKE CONCAT('%', #{userInfo}, '%')")
    List<UserDto> findUserByInfo(String userInfo);
}
