package com.example.chatserver.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.dto.QrCodeResult;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.SecurityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class QrCodeService {

    @Resource
    RedisUtils redisUtils;

    @Resource
    UserService userService;

    public String createQrCode(String action, String userIp, String userId) {
        switch (action) {
            case "login": {
                QrCodeResult qrCodeResult = new QrCodeResult();
                qrCodeResult.setAction("login");
                qrCodeResult.setIp(userIp);
                qrCodeResult.setStatus("wait");
                String key = IdUtil.objectId();
                redisUtils.set(key, JSONUtil.toJsonStr(qrCodeResult), 60);
                return key;
            }
            case "mine": {
                if (userId == null) {
                    throw new BaseException("用户不能为空~");
                }
                QrCodeResult qrCodeResult = new QrCodeResult();
                qrCodeResult.setAction("mine");
                qrCodeResult.setIp(userIp);
                UserDto user = userService.info(userId);
                qrCodeResult.setExtend(JSONUtil.parseObj(user));
                String key = SecurityUtil.aesEncrypt(userId);
                redisUtils.set(key, JSONUtil.toJsonStr(qrCodeResult), 30 * 24 * 60 * 60);
                return key;
            }
        }
        throw new BaseException("二维码生成失败~");
    }
}
