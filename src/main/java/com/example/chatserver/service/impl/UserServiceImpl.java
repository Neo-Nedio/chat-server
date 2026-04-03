package com.example.chatserver.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.User;
import com.example.chatserver.login.LoginVo;
import com.example.chatserver.mapper.UserMapper;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.JwtUtil;
import com.example.chatserver.utils.ResultUtil;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public JSONObject validateLogin(LoginVo loginVo) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, loginVo.getAccount()); //添加条件
        User user = getOne(queryWrapper); //执行查询，返回匹配的第一个用户

        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误");
        }
        if (!user.getPassword().equals(loginVo.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误");
        }

        JSONObject userinfo = new JSONObject();
        userinfo.set("userId", user.getId());
        userinfo.set("account", user.getAccount());
        userinfo.set("username", user.getName());
        userinfo.set("phone", user.getPhone());
        userinfo.set("email", user.getEmail());
        //生成用户token
        userinfo.set("token", JwtUtil.createToken(userinfo));
        return ResultUtil.Succeed(userinfo);
    }
}
