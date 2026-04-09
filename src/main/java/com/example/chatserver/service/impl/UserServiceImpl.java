package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.admin.vo.user.*;
import com.example.chatserver.config.MinioConfig;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.constant.UserStatus;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.service.EmailService;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.utils.RedisUtils;
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
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

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
    EmailService emailService;

    @Resource
    UserMapper userMapper;

    @Resource
    MinioConfig minioConfig;

    @Resource
    RedisUtils redisUtils;

    @Override
    public boolean register(RegisterVo registerVo) {
        //验证码校验
        String code = (String) redisUtils.get(registerVo.getEmail());
        if (code == null || !code.equals(registerVo.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(registerVo.getEmail());
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
        user.setStatus(UserStatus.Normal);
        user.setSex("男");
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");
        return save(user);
    }


    @Override
    public JSONObject validateLogin(LoginVo loginVo, boolean isAdmin) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, loginVo.getAccount()); //添加条件
        User user = getOne(queryWrapper); //执行查询，返回匹配的第一个用户

        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if (!SecurityUtil.verifyPassword(loginVo.getPassword(), user.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误~");
        }

        if (isAdmin && !UserRole.Admin.equals(user.getRole())) {
            return ResultUtil.Fail("您非管理员~");
        }

        JSONObject userinfo = new JSONObject();
        userinfo.set("userId", user.getId());
        userinfo.set("account", user.getAccount());
        userinfo.set("username", user.getName());
        userinfo.set("role", user.getRole());
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

    @Override
    public Page<User> userList(UserListVo userListVo) {
        Page<User> page = new Page<>(userListVo.getCurrentPage(), userListVo.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(userListVo.getKeyword())) {
            queryWrapper.and(query -> {
                query.like(User::getName, userListVo.getKeyword())
                        .or()
                        .like(User::getAccount, userListVo.getKeyword())
                        .or()
                        .like(User::getEmail, userListVo.getKeyword())
                        .or()
                        .like(User::getPhone, userListVo.getKeyword());
            });
        }
        if (StringUtils.isNotBlank(userListVo.getOnlineStatus())) {
            if (userListVo.getOnlineStatus().equals("online")) {
                queryWrapper.eq(User::getIsOnline, true);
            }
            if (userListVo.getOnlineStatus().equals("offline")) {
                queryWrapper.eq(User::getIsOnline, false);
            }
        }
        queryWrapper.orderByDesc(User::getCreateTime);
        return this.page(page, queryWrapper);
    }

    @Override
    public void offline(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, false)
                .eq(User::getId, userId);
        update(updateWrapper);
    }

    @Override
    public void online(String userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, true)
                .eq(User::getId, userId);
        update(updateWrapper);
    }

    @Override
    public boolean createUser(CreateUserVo createUserVo) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, createUserVo.getAccount());
        if (count(queryWrapper) > 0) {
            throw new BaseException("账号已存在~");
        }
        queryWrapper.clear();
        queryWrapper.eq(User::getEmail, createUserVo.getEmail());
        if (count(queryWrapper) > 0) {
            throw new BaseException("邮箱已存在~");
        }

        //创建用户
        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(createUserVo.getUsername());
        user.setAccount(createUserVo.getAccount());
        String password = RandomUtil.randomString(8);
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setSex("男");
        user.setEmail(createUserVo.getEmail());
        user.setPortrait(minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/default-portrait.jpg");

        //密码发送邮件
        if (save(user)) {
            Context context = new Context();
            context.setVariable("username", createUserVo.getUsername());
            context.setVariable("account", createUserVo.getAccount());
            context.setVariable("password", password);
            try {
                //发送邮件
                emailService.sendHtmlMessage(createUserVo.getEmail(), "用户密码", "email_password_template.html", context);
            } catch (MessagingException e) {
                log.error(e.getMessage());
            }
        }

        return true;
    }

    @Override
    public boolean allUserOffline() {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getIsOnline, false);
        return update(updateWrapper);
    }

    @Override
    public boolean disableUser(String userId, DisableUserVo disableUserVo) {
        if (userId.equals(disableUserVo.getUserId())) {
            throw new BaseException("不能禁用自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        //todo 禁用后不允许上线
        updateWrapper.set(User::getStatus, UserStatus.Disable)
                .eq(User::getId, disableUserVo.getUserId());
        return update(updateWrapper);
    }

    @Override
    public boolean deleteUser(String userId, DeleteUserVo deleteUserVo) {
        if (userId.equals(deleteUserVo.getUserId())) {
            throw new BaseException("不能删除自己~");
        }
        return removeById(deleteUserVo.getUserId());
    }

    @Override
    public boolean unDisableUser(UnDisableUserVo unDisableUserVo) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getStatus, UserStatus.Normal)
                .eq(User::getId, unDisableUserVo.getUserId());
        return update(updateWrapper);
    }
}
