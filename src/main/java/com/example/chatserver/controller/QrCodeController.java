package com.example.chatserver.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserIp;
import com.example.chatserver.dto.QrCodeResult;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.qr.ResultVo;
import com.example.chatserver.vo.qr.StatusVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/qr")
@Slf4j
public class QrCodeController {

    @Resource
    RedisUtils redisUtils;

    /*PC：GET /qr/code → 得到 key → 展示二维码。
    手机：扫码得到 key → 已登录状态下 POST /v1/api/login/qr，body 带 key。
    服务端：在 Redis 里把该 key 对应数据更新为 success 并写入 userInfo（含 token）。
    PC：轮询 POST /qr/code/result，同 IP 校验通过后，从返回的 QrCodeResult.userInfo 里取 token，完成 Web 登录。*/

    @GetMapping("/code")
    @UrlFree
    //客户端打开二维码
    public JSONObject code(@UserIp String userIp) {
        QrCodeResult qrCodeResult = new QrCodeResult();
        qrCodeResult.setAction("login");
        qrCodeResult.setIp(userIp);
        qrCodeResult.setStatus("wait");
        String key = IdUtil.objectId();
        redisUtils.set(key, JSONUtil.toJsonStr(qrCodeResult), 1);
        return ResultUtil.Succeed(key);
    }

    @PostMapping("/code/result")
    @UrlFree
    //获取客户端二维码的结果(要判断ip，以此判断是否同一设备)，从中取出token
    //要先在LoginController中将token放入客户端对应的qrCodeResult
    public JSONObject result(@UserIp String userIp, @RequestBody ResultVo resultVo) {
        String result = (String) redisUtils.get(resultVo.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        if (!userIp.equals(qrCodeResult.getIp())) {
            throw new BaseException("登录地址不匹配~");
        }
        return ResultUtil.Succeed(qrCodeResult);
    }

    @GetMapping("/code/status")
    //已扫码
    public JSONObject status(@RequestBody StatusVo statusVo) {
        String result = (String) redisUtils.get(statusVo.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        qrCodeResult.setStatus("scan");
        redisUtils.set(statusVo.getKey(), JSONUtil.toJsonStr(qrCodeResult), 1);
        return ResultUtil.Succeed(qrCodeResult);
    }
}
