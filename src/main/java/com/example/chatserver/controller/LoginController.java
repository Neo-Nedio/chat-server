package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserIp;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/api/login")
@Slf4j
public class LoginController {
    @Resource
    UserService userService;

    @UrlFree
    @GetMapping("/public-key")
    public Object getPublicKey() {
        String result = SecurityUtil.getPublicKey();
        return ResultUtil.Succeed(result);
    }

    @UrlFree
    @PostMapping()
    public Object login(@Valid @RequestBody LoginVo loginVo, @UserIp String userIp) {
        String decryptedPassword = SecurityUtil.decryptPassword(loginVo.getPassword());
        loginVo.setPassword(decryptedPassword);
        return userService.validateLogin(loginVo,userIp,false);
    }
}
