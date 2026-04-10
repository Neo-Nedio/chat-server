package com.example.chatserver.admin.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserIp;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.login.LoginVo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("AdminLoginController")
@RequestMapping("/admin/v1/api/login")
@Slf4j
public class LoginController {

    @Resource
    UserService userService;

    @UrlFree
    @PostMapping()
    public Object login(@Valid @RequestBody LoginVo loginVo, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginVo.getPassword());
        loginVo.setPassword(decryptedPassword);
        return userService.validateLogin(loginVo, userIp, true);
    }
}
