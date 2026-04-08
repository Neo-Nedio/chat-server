package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.config.MinioConfig;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.mapper.UserMapper;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.JwtUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.user.RegisterVo;
import com.example.chatserver.vo.user.SearchUserVo;
import com.example.chatserver.vo.user.UpdateVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    ChatListService chatListService;

    @Resource
    NotifyService notifyService;

    @Resource
    UserMapper userMapper;

    @Resource
    MinioConfig minioConfig;

    @Override
    public boolean register(RegisterVo registerVo) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, registerVo.getAccount());
        if (count(queryWrapper) > 0) {
            throw new BaseException("账号已存在~");
        }

        queryWrapper.clear();
        queryWrapper.eq(User::getEmail, registerVo.getEmail());
        if (count(queryWrapper) > 0) {
            throw new BaseException("邮箱已存在~");
        }

        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(registerVo.getUsername());
        user.setAccount(registerVo.getAccount());
        user.setPassword(registerVo.getPassword());
        user.setBirthday(new Date());
        user.setSex("男");
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");
        return save(user);
    }


    @Override
    public JSONObject validateLogin(LoginVo loginVo) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, loginVo.getAccount()); //添加条件
        User user = getOne(queryWrapper); //执行查询，返回匹配的第一个用户

        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误");
        }
        if (!SecurityUtil.verifyPassword(loginVo.getPassword(), user.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误");
        }

        JSONObject userinfo = new JSONObject();
        userinfo.set("userId", user.getId());
        userinfo.set("account", user.getAccount());
        userinfo.set("username", user.getName());
        userinfo.set("portrait", user.getPortrait());
        userinfo.set("phone", user.getPhone());
        userinfo.set("email", user.getEmail());
        //生成用户token
        userinfo.set("token", JwtUtil.createToken(userinfo));
        return ResultUtil.Succeed(userinfo);
    }

    @Override
    //搜索用户
    public List<UserDto> searchUser(SearchUserVo searchUserVo) {
        return userMapper.findUserByInfo(searchUserVo.getUserInfo());
    }

    @Override
    //获取用户所有未读通知
    public HashMap<String, Integer> unreadInfo(String userId) {
        HashMap<String, Integer> unreadInfo = new HashMap<>();
        //获取消息未读数
        int msgNum = chatListService.unread(userId);
        //获取通知未读数
        int notifyNum = notifyService.unread(userId);
        unreadInfo.put("chat", msgNum);
        unreadInfo.put("notify", notifyNum);
        return unreadInfo;
    }

    @Override
    public UserDto info(String userId) {
        return userMapper.info(userId);
    }

    @Override
    public boolean updateUserInfo(String userId, UpdateVo updateVo) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName, updateVo.getName())
                .set(User::getPortrait, updateVo.getPortrait())
                .set(User::getSex, updateVo.getSex())
                .set(User::getBirthday, updateVo.getBirthday())
                .set(User::getSignature, updateVo.getSignature())
                .eq(User::getId, userId);
        return update(updateWrapper);
    }

    @Override
    public boolean updateUserPortrait(String userId, String portrait) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getPortrait, portrait)
                .eq(User::getId, userId);
        return update(updateWrapper);
    }
}
