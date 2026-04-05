package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.GroupListDto;
import com.example.chatserver.entity.Group;
import com.example.chatserver.vo.group.CreateGroupVo;
import com.example.chatserver.vo.group.DeleteGroupVo;
import com.example.chatserver.vo.group.UpdateGroupVo;

import java.util.List;


public interface GroupService extends IService<Group> {

    List<Group> getGroupByUserId(String userId);

    boolean createGroup(String userId, CreateGroupVo createGroupVo);

    boolean updateGroup(String userId, UpdateGroupVo updateGroupVo);

    boolean deleteGroup(String userId, DeleteGroupVo deleteGroupVo);

    List<GroupListDto> getList(String userId);

    boolean IsExistGroupByUserId(String userId, String GroupId);
}
