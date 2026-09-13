package com.example.chatserver.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.admin.vo.user.*;
import com.example.chatserver.config.MinioConfig;
import com.example.chatserver.constant.NotifyType;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.constant.UserStatus;
import com.example.chatserver.dto.QrCodeResult;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.*;
import com.example.chatserver.utils.*;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.mapper.UserMapper;
import com.example.chatserver.vo.login.QrCodeLoginVo;
import com.example.chatserver.vo.user.*;
import com.example.chatserver.websocket.WebSocketService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    UserOperatedService userOperatedService;

    @Resource
    WebSocketService webSocketService;

    @Resource
    VerificationCodeService verificationCodeService;

    @Resource
    UserMapper userMapper;

    @Resource
    MinioConfig minioConfig;

    @Resource
    RedisUtils redisUtils;

    @Resource
    MinioUtil minioUtil;


    @Override
    public boolean register(RegisterVo registerVo) {
        //验证码校验
        String code = (String) redisUtils.get(registerVo.getEmail());
        if (code == null || !code.equals(registerVo.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }

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

        redisUtils.del(registerVo.getEmail());

        User user = new User();
        user.setId(IdUtil.randomUUID());
        user.setName(registerVo.getUsername());
        user.setAccount(registerVo.getAccount());
        String passwordHash = SecurityUtil.hashPassword(registerVo.getPassword());
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setSex("男");
        user.setEmail(registerVo.getEmail());
        user.setRole(UserRole.User);
        return save(user);
    }


    @Override
    public JSONObject validateLogin(LoginVo loginVo, String userIp, boolean isAdmin) {
        // 获取用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, loginVo.getAccount()); //添加条件
        User user = getOne(queryWrapper); //执行查询，返回匹配的第一个用户

        if (null == user) {
            return ResultUtil.Fail("用户名或密码错误~");
        }
        if(user.getStatus().equals(UserStatus.Disable)){
            return ResultUtil.Fail("您的账号已被管理员禁用，请联系管理员处理");
        }
        if (!SecurityUtil.verifyPassword(loginVo.getPassword(), user.getPassword())) {
            return ResultUtil.Fail("用户名或密码错误~");
        }

        if (isAdmin && !UserRole.Admin.equals(user.getRole())) {
            return ResultUtil.Fail("您非管理员~");
        }

        //获取token
        JSONObject userinfo = createUserToken(user, userIp);
        user.setOnlineEquipment(loginVo.getOnlineEquipment());
        user.setPushyToken(loginVo.getPushyToken());
        boolean isSave = updateById(user);
        return isSave?ResultUtil.Succeed(userinfo):ResultUtil.Fail("登录失败~");
    }

    public JSONObject createUserToken(User user, String userIp) {

        JSONObject userinfo = new JSONObject();
        userinfo.set("userId", user.getId());
        userinfo.set("account", user.getAccount());
        userinfo.set("username", user.getName());
        userinfo.set("role", user.getRole());
        userinfo.set("portrait", user.getPortrait());
        userinfo.set("sex", user.getSex());
        userinfo.set("phone", user.getPhone());
        userinfo.set("email", user.getEmail());
        userinfo.set("status",user.getStatus());
        //生成用户token
        userinfo.set("token", JwtUtil.createToken(userinfo));

        ThreadUtil.execAsync(() -> {
            //记录登录操作
            userOperatedService.recordLogin(user.getId(), userIp);
            //更新同时在线人数
            updateRedisOnlineNum();
        });
        return userinfo;
    }

    public void updateRedisOnlineNum() {
        Integer onlineNum = webSocketService.getOnlineNum();
        String key = "onlineNum#" + DateUtil.today();
        Integer redisOnlineNum = (Integer) redisUtils.get(key);
        if (null == redisOnlineNum) {
            redisUtils.set(key, onlineNum, 25 * 60 * 60);
        }
        if (onlineNum > redisOnlineNum) {
            redisUtils.set(key, onlineNum, 25 * 60 * 60);
        }
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
        unreadInfo.put("friendNotify", notifyService.unreadByType(userId, NotifyType.Friend_Apply));
        unreadInfo.put("groupNotify", notifyService.unreadByType(userId, NotifyType.Group_Apply));
        unreadInfo.put("systemNotify", notifyService.unreadByType(userId, NotifyType.System));
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
        boolean ok = update(updateWrapper);
        if (ok) redisUtils.del("user:" + userId);
        return ok;
    }

    @Override
    public boolean updateUserInfo(String userId, UpdatePasswordVo updateVo) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        String passwordHash = SecurityUtil.hashPassword(updateVo.getConfirmPassword());
        updateWrapper.set(User::getPassword, passwordHash)
                .eq(User::getId, userId);
        boolean ok = update(updateWrapper);
        if (ok) redisUtils.del("user:" + userId);
        return ok;
    }
    @Override
    public boolean updateUserPortrait(String userId, String portrait) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getPortrait, portrait)
                .eq(User::getId, userId);
        boolean ok = update(updateWrapper);
        if (ok) redisUtils.del("user:" + userId);
        return ok;
    }

    @Override
    public boolean forget(ForgetVo forgetVo) {
        User user = getUserByAccount(forgetVo.getAccount());
        if (null == user) throw new BaseException("用户不存在~");
        //验证码校验
        String code = (String) redisUtils.get(user.getEmail());
        if (code == null || !code.equals(forgetVo.getCode())) {
            throw new BaseException("验证码错误或者已失效~");
        }
        redisUtils.del(user.getEmail());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, forgetVo.getAccount())
                .eq(User::getEmail, user.getEmail());
        String passwordHash = SecurityUtil.hashPassword(forgetVo.getPassword());
        user.setPassword(passwordHash);
        return updateById(user);
    }

    @Override
    public Page<User> userList(UserListVo userListVo) {
        Page<User> page = new Page<>(userListVo.getCurrentPage(), userListVo.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //只查询需要的字段，排除敏感字段（如密码）
        queryWrapper.select(User::getId, User::getAccount, User::getName, User::getPortrait,
                User::getSex, User::getBirthday, User::getSignature, User::getPhone,
                User::getEmail, User::getLastOptTime, User::getStatus, User::getIsOnline,
                User::getRole, User::getCreateTime, User::getUpdateTime);
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
        user.setName(createUserVo.getName());
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
            context.setVariable("username", createUserVo.getName());
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
        updateWrapper.set(User::getStatus, UserStatus.Disable)
                .eq(User::getId, disableUserVo.getUserId());
        //先 update 数据库，成功后再 sendDisableToUser。确保数据状态和通知行为一致。
        boolean success = update(updateWrapper);
        if (success) {
            webSocketService.sendDisableToUser(disableUserVo.getUserId());
        }
        return success;
    }

    @Override
    public boolean deleteUser(String userId, DeleteUserVo deleteUserVo) {
        if (userId.equals(deleteUserVo.getUserId())) {
            throw new BaseException("不能删除自己~");
        }
        User user = getById(deleteUserVo.getUserId());
        if (UserRole.Third.equals(user.getRole())) {
            throw new BaseException("第三方用户不能删除，请到会话中删除~");
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

    @Override
    public boolean updateUser(UpdateUserVo updateUserVo) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, updateUserVo.getEmail());
        if (count(queryWrapper) > 0) {
            throw new BaseException("邮箱已存在~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getName, updateUserVo.getName())
                .set(User::getEmail, updateUserVo.getEmail())
                .set(User::getPhone, updateUserVo.getPhone())
                .eq(User::getId, updateUserVo.getId());
        boolean ok = update(updateWrapper);
        if (ok) redisUtils.del("user:" + updateUserVo.getId());
        return ok;
    }

    @Override
    public String restPassword(ResetPasswordVo resetPasswordVo) {
        User user = getById(resetPasswordVo.getUserId());
        if (null == user) {
            throw new BaseException("用户不存在~");
        }
        String password = RandomUtil.randomString(4);
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setPassword(passwordHash);
        //密码发送邮件
        if (updateById(user)) {
            Context context = new Context();
            context.setVariable("username", user.getName());
            context.setVariable("account", user.getAccount());
            context.setVariable("password", password);
            try {
                //发送邮件
                emailService.sendHtmlMessage(user.getEmail(), "用户密码", "email_password_template.html", context);
            } catch (MessagingException e) {
                log.error(e.getMessage());
            }
        }
        return password;
    }

    @Override
    public boolean setAdmin(String userId, SetAdminVo setAdminVo) {
        if (userId.equals(setAdminVo.getUserId())) {
            throw new BaseException("不能操作自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getRole, UserRole.Admin)
                .eq(User::getId, setAdminVo.getUserId());
        return update(updateWrapper);
    }

    @Override
    public boolean cancelAdmin(String userId, CancelAdminVo cancelAdminVo) {
        if (userId.equals(cancelAdminVo.getUserId())) {
            throw new BaseException("不能操作自己~");
        }
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(User::getRole, UserRole.User)
                .eq(User::getId, cancelAdminVo.getUserId());
        return update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String createThirdPartyUser(MultipartFile portrait, String name) {
        String userId = IdUtil.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setName(name);
        user.setAccount(IdUtil.objectId());
        String password = RandomUtil.randomString(8); //随机密码，不需要知道，第三方用密钥登录
        String passwordHash = SecurityUtil.hashPassword(password);
        user.setStatus(UserStatus.Normal);
        user.setPassword(passwordHash);
        user.setBirthday(new Date());
        user.setRole(UserRole.Third);
        user.setSex("男");
        String url;
        try {
            url = minioUtil.upload(portrait.getInputStream(), userId + "-portrait"
                    , portrait.getContentType(), portrait.getSize());
        } catch (Exception e) {
            throw new BaseException("头像上传失败~");
        }
        user.setPortrait(url);
        save(user);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateThirdPartyUser(MultipartFile portrait, String name, String userId) {
        String url;
        try {
            url = minioUtil.upload(portrait.getInputStream(), userId + "-portrait"
                    , portrait.getContentType(), portrait.getSize());
        } catch (Exception e) {
            throw new BaseException("头像上传失败~");
        }
        url += "?t=" + System.currentTimeMillis();
        User user = getById(userId);
        user.setPortrait(url);
        user.setName(name);
        boolean ok = updateById(user);
        if (ok) redisUtils.del("user:" + userId);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteThirdPartyUser(String userId) {
        return removeById(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return getOne(queryWrapper);
    }

    @Override
    public User getUserByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        return getOne(queryWrapper);
    }

    @Override
    public JSONObject validateQrCodeLogin(QrCodeLoginVo qrCodeLoginVo, String userid) {
        // 获取用户
        User user = getById(userid);
        if (null == user) {
            return ResultUtil.Fail("用户不存在~");
        }

        String result = (String) redisUtils.get(qrCodeLoginVo.getKey());
        if (null == result) {
            return ResultUtil.Fail("二维码已失效~");
        }

        //将用户保存在对应客户端的QrCodeResult
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        JSONObject userinfo = createUserToken(user, qrCodeResult.getIp());
        qrCodeResult.setStatus("success");
        qrCodeResult.setExtend(userinfo);
        redisUtils.set(qrCodeLoginVo.getKey(), JSONUtil.toJsonStr(qrCodeResult), 60);
        return ResultUtil.Succeed();
    }

    @Override
    //通过账号获取邮箱验证码
    public void emailVerifyByAccount(String account) {
        User user = getUserByAccount(account);
        if (null == user) {
            throw new BaseException("用户不存在~");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            throw new BaseException("用户没有对应的邮箱~");
        }
        verificationCodeService.emailVerificationCode(user.getEmail());
    }
}
