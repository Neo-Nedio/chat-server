package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.User;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.vo.user.SearchUserVo;

import java.util.HashMap;
import java.util.List;

public interface UserService extends IService<User> {
    JSONObject validateLogin(LoginVo loginVo);

    List<User> searchUser(SearchUserVo searchUserVo);

    HashMap<String, Integer> unreadInfo(String userId);
}
