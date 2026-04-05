package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.Group;
import com.example.chatserver.mapper.GroupMapper;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.GroupService;
import com.example.chatserver.vo.group.CreateGroupVo;
import com.example.chatserver.vo.group.DeleteGroupVo;
import com.example.chatserver.vo.group.UpdateGroupVo;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Lazy
    @Resource
    FriendService friendService;

    @Override
    //查询相应用户的所有分组
    public List<Group> getGroupByUserId(String userId) {
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getUserId, userId).orderByAsc(Group::getName);
        return list(queryWrapper);
    }

    @Override
    public boolean createGroup(String userId, CreateGroupVo createGroupVo) {
        Group group = new Group();
        group.setId(IdUtil.randomUUID());
        group.setUserId(userId);
        group.setName(createGroupVo.getGroupName());
        return save(group);
    }

    @Override
    public boolean updateGroup(String userId, UpdateGroupVo updateGroupVo) {
        LambdaUpdateWrapper<Group> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Group::getName, updateGroupVo.getGroupName())
                .eq(Group::getUserId, userId)
                .eq(Group::getId, updateGroupVo.getGroupId());
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteGroup(String userId, DeleteGroupVo deleteGroupVo) {
        //将该分组下好友设置为未分组
        //userId是设置了这个分组的人的id,不是好友id,所以能找到这个分组下的所有好友
        friendService.updateGroupId(userId, deleteGroupVo.getGroupId(), "0");
        //删除分组
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getId, deleteGroupVo.getGroupId())
                .eq(Group::getUserId, userId);
        return remove(queryWrapper);
    }
}
