package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.TalkPermission;
import com.example.chatserver.mapper.TalkPermissionMapper;
import com.example.chatserver.service.TalkPermissionService;
import org.springframework.stereotype.Service;


@Service
public class TalkPermissionServiceImpl extends ServiceImpl<TalkPermissionMapper, TalkPermission> implements TalkPermissionService {

}
