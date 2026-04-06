package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper extends BaseMapper<User> {

    @Select("select * from `user` where `id` = #{userId}")
    UserDto info(String userId);

    @Select("SELECT * FROM user WHERE account = #{userInfo} OR phone = #{userInfo} OR email = #{userInfo}")
    List<UserDto> findUserByInfo(String userInfo);
}
