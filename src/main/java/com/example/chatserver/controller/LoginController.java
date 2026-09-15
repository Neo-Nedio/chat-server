package com.example.chatserver.controller;

import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserIp;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.utils.SecurityUtil;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.service.UserService;
import com.example.chatserver.vo.login.QrCodeLoginVo;
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

    @PostMapping("/logout")
    public Object logout(@Userid String userId, @RequestHeader("x-token") String token) {
        userService.logout(userId, token);
        return ResultUtil.Succeed();
    }

    @PostMapping("/qr")
    //移动端扫描二维码
    //userId 为移动端当前账号，将当前账号绑定到客户端二维码
    public Object qrCodeLogin(@Valid @RequestBody QrCodeLoginVo qrCodeLoginVo, @Userid String userid) {
        return userService.validateQrCodeLogin(qrCodeLoginVo, userid);
    }
}
