package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.GroupListDto;
import com.example.chatserver.entity.Group;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface GroupMapper extends BaseMapper<Group> {
    @Select("SELECT `name` AS `label`,`id` AS `value` FROM `group` WHERE `user_id` = #{userId}")
    List<GroupListDto> getList(String userId);
}
