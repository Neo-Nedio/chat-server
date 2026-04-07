package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.UserSet;


public interface UserSetService extends IService<UserSet> {

    UserSet getUserSet(String userId);
}
