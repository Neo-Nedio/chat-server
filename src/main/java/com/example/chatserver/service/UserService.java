package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.User;
import com.example.chatserver.vo.LoginVo;

public interface UserService extends IService<User> {
    JSONObject validateLogin(LoginVo loginVo);
}
