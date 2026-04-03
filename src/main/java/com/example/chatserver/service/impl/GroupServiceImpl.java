package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.Group;
import com.example.chatserver.mapper.GroupMapper;
import com.example.chatserver.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Override
    //查询相应用户的所有分组
    public List<Group> getGroupByUserId(String userId) {
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getUserId, userId).orderByAsc(Group::getName);
        return list(queryWrapper);
    }
}
