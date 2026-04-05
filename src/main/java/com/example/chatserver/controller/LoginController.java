package com.example.chatserver.controller;

import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/api/login")
@Slf4j
public class LoginController {
    @Resource
    UserService userService;

    @UrlFree
    @PostMapping()
    public Object login(@Valid @RequestBody LoginVo loginVo) {
        return userService.validateLogin(loginVo);
    }
}
