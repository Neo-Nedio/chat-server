package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserRole;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public JSONObject register(@RequestBody RegisterVo registerVo) {
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
        String fileName = userId + "-portrait" + name.substring(name.lastIndexOf("."));
        String url = minioUtil.upload(request.getInputStream(), fileName, type, size);
        url += "?t=" + System.currentTimeMillis();
        userService.updateUserPortrait(userId, url);
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
        boolean isFriend = friendService.isFriend(userId, targetId);
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
        boolean isFriend = friendService.isFriend(userId, targetId);
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
}
