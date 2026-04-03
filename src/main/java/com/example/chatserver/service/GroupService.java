package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.Group;

import java.util.List;


public interface GroupService extends IService<Group> {

    List<Group> getGroupByUserId(String userId);
}
