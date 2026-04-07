package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.UserSet;
import com.example.chatserver.vo.userSet.UpdateUserSetVo;


public interface UserSetService extends IService<UserSet> {

    UserSet getUserSet(String userId);

    boolean updateUserSet(String userId, UpdateUserSetVo updateUserSetVo);
}
