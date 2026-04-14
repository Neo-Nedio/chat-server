package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserRole;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.entity.User;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.service.VerificationCodeService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.user.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Printable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/v1/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @Resource
    FriendService friendService;

    @Resource
    VerificationCodeService verificationCodeService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

    /**
     * 用户注册
     */
    @UrlFree
    @PostMapping("/register")
    public JSONObject register(@Valid @RequestBody RegisterVo registerVo) {
        //RSA 解密
        String decryptedPassword = SecurityUtil.decryptPassword(registerVo.getPassword());
        registerVo.setPassword(decryptedPassword);
        boolean result = userService.register(registerVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 用户查询
     */
    @PostMapping("/search")
    public JSONObject searchUser(@RequestBody SearchUserVo searchUserVo) {
        List<UserDto> result = userService.searchUser(searchUserVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取用户每项未读数
     */
    @GetMapping("/unread")
    public JSONObject unreadInfo(@Userid String userId) {
        HashMap<String, Integer> result = userService.unreadInfo(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 邮箱验证码
     */
    @PostMapping("/email/verify")
    @UrlFree
    public JSONObject emailVerify(@RequestBody EmailVerifyVo emailVerifyVo) {
        verificationCodeService.emailVerificationCode(emailVerifyVo.getEmail());
        return ResultUtil.Succeed();
    }

    /**
     * 邮箱验证码(通过账号)
     */
    @PostMapping("/email/verify/by/account")
    @UrlFree
    public JSONObject emailVerifyByAccount(@RequestBody EmailVerifyByAccountVo emailVerifyByAccountVo) {
        userService.emailVerifyByAccount(emailVerifyByAccountVo.getAccount());
        return ResultUtil.Succeed();
    }

    /**
     * 忘记密码

     */
    @UrlFree
    @PostMapping("/forget")
    public JSONObject forget(@RequestBody ForgetVo forgetVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(forgetVo.getPassword());
        forgetVo.setPassword(decryptedPassword);
        boolean result = userService.forget(forgetVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     *修改密码
     */
    @PostMapping("/update/password")
    public JSONObject updateUserPassword(@Userid String userId, @RequestBody UpdatePasswordVo updateVo) {
        String decryptedPassword = SecurityUtil.decryptPassword(updateVo.getConfirmPassword());
        updateVo.setConfirmPassword(decryptedPassword);
        User user = userService.getById(userId);
        //验证旧密码是否正确
        if (SecurityUtil.verifyPassword(updateVo.getOldPassword(), user.getPassword())) {
            boolean result = userService.updateUserInfo(userId, updateVo);
            return ResultUtil.ResultByFlag(result);
        }
        else return ResultUtil.ResultByFlag(false,"原密码错误~",400);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public JSONObject info(@Userid String userId) {
        UserDto result = userService.info(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改当前用户信息
     */
    @PostMapping("/update")
    public JSONObject update(@Userid String userId, @RequestBody UpdateVo updateVo) {
        boolean result = userService.updateUserInfo(userId, updateVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 上传头像
     */
    @PostMapping(value = "/upload/portrait")
    public JSONObject upload(HttpServletRequest request,
                             @Userid String userId,
                             @RequestHeader("name") String name,
                             @RequestHeader("type") String type,
                             @RequestHeader("size") long size) throws IOException {
        //用时间戳让文件名不一样，从而url不一样，这样前端就不会因为url一样用原缓存
        String fileName = userId + "-portrait" + System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        minioUtil.upload(request.getInputStream(), fileName, type, size);
        userService.updateUserPortrait(userId, fileName);
        return ResultUtil.Succeed(fileName);
    }

    @PostMapping(value = "upload/portrait/form")
    public JSONObject uploadFrom(@Userid String userId,
                                 @RequestParam("name") String name,
                                 @RequestParam("type") String type,
                                 @RequestParam("size") long size,
                                 @RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getById(userId);
        if (user == null) {
            return ResultUtil.Fail("用户不存在");
        }
        if(StringUtils.isNotBlank(user.getPortrait())){
            minioUtil.remove(user.getPortrait());
        }
        //用时间戳让文件名不一样，从而url不一样，这样前端就不会因为url一样用原缓存
        String fileName = userId + "-portrait" + System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        minioUtil.upload(file.getInputStream(), fileName, type, size);
        userService.updateUserPortrait(userId, fileName);
        return ResultUtil.Succeed(fileName);
    }

    /**
     * 获取头像
     */
    @GetMapping("/get/portrait")
    public JSONObject getMedia(@Userid String userId, @RequestParam("fileName") String fileName) {
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.preview(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }

    /**
     * 获取文件
     */
    @GetMapping("/get/file")
    public ResponseEntity<InputStreamResource> getFile(@Userid String userId,
                                                       @UserRole String role,
                                                       @RequestHeader("targetId") String targetId,
                                                       @RequestHeader("fileName") String fileName) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, targetId);
        if (!isFriend && !userId.equals(targetId) &&
                com.example.chatserver.constant.UserRole.User.equals(role)) {
            throw new BaseException("双方非好友");
        }
        InputStream inputStream = minioUtil.getObject(targetId + "/img/" + fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 获取图片内容
     */
    @GetMapping("/get/img")
    public JSONObject getImg(@Userid String userId,
                             @UserRole String role,
                             @RequestParam("targetId") String targetId,
                             @RequestParam("fileName") String fileName) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, targetId);
        if (!isFriend && !userId.equals(targetId) &&
                com.example.chatserver.constant.UserRole.User.equals(role)) {
            throw new BaseException("双方非好友");
        }
        String name = targetId + "/img/" + fileName;
        String url = (String) redisUtils.get(name);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.previewFile(name);
            redisUtils.set(name, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }

    /**
     * 设置聊天背景
     */
    @PostMapping("/set-chat-background")
    public JSONObject setChatBackground(@Userid String userId,
                                        @RequestParam("name") String name,
                                        @RequestParam("type") String type,
                                        @RequestParam("size") long size,
                                        @RequestParam("file") MultipartFile file) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResultUtil.Fail("用户不存在");
        }
        if(StringUtils.isNotBlank(user.getChatBackground())){
            minioUtil.remove(user.getChatBackground());
        }
        boolean update;
        String url;
        try {
            String fileName = userId + "-chat-background-" + System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
            url = minioUtil.upload(file.getInputStream(), fileName, type, size);
            user.setChatBackground(fileName);
            update = userService.updateById(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!update) return ResultUtil.Fail("设置失败");
        return ResultUtil.Succeed(url);
    }

    /**
     * 获取聊天背景
     */
    @GetMapping("/get-chat-background")
    public JSONObject getChatBackground(@Userid String userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResultUtil.Fail("用户不存在");
        }
        String fileName = user.getChatBackground();
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.preview(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60 * 60);
        }
        return ResultUtil.Succeed(url);
    }
}
