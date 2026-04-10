package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.UserIp;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.QrCodeResult;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.QrCodeService;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.qr.ResultVo;
import com.example.chatserver.vo.qr.StatusVo;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Path;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/qr")
@Slf4j
public class QrCodeController {

    @Resource
    RedisUtils redisUtils;

    @Resource
    QrCodeService qrCodeService;

    /* 扫码登录
    PC：GET /qr/code → 得到 key → 展示二维码。
    手机：扫码得到 key → 已登录状态下 POST /v1/api/login/qr，body 带 key。
    服务端：在 Redis 里把该 key 对应数据更新为 success 并写入 userInfo（含 token）。
    PC：轮询 POST /qr/code/result，从返回的 QrCodeResult.userInfo 里取 token，完成 Web 登录。*/

    @GetMapping("/code")
    @UrlFree
    public JSONObject code(@UserIp String userIp, @Userid String userId, @Path("action") String action) {
        String key = qrCodeService.createQrCode(action, userIp, userId);
        return ResultUtil.Succeed(key);
    }

    @PostMapping("/code/result")
    @UrlFree
    //一般给客户端轮询使用，移动端在status接口就已经知道QrCodeResult了
    public JSONObject result(@RequestBody ResultVo resultVo) {
        String result = (String) redisUtils.get(resultVo.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        return ResultUtil.Succeed(qrCodeResult);
    }

    @GetMapping("/code/status")
    //已扫码
    //这个接口不止可以设置状态，还可以获取qrCodeResult
    //这样当移动端扫码时，如果是添加好友的key,就可以用qrCodeResult取出用户信息再找到id添加
    //如果是登录客户端的key，也可以用qrCodeResult里的action转向同意登录的页面
    public JSONObject status(@RequestBody StatusVo statusVo) {
        String result = (String) redisUtils.get(statusVo.getKey());
        if (null == result) {
            throw new BaseException("二维码失效~");
        }
        QrCodeResult qrCodeResult = JSONUtil.toBean(result, QrCodeResult.class);
        qrCodeResult.setStatus("scan");

        // 1. 获取 key 的剩余过期时间（秒）
        long ttl = redisUtils.getExpire(statusVo.getKey());

        // 2. 判断是否还有效
        if (ttl > 0) {
            // 还有剩余时间：重新设置，保持原来的过期时间
            redisUtils.set(statusVo.getKey(), JSONUtil.toJsonStr(qrCodeResult), ttl);
        } else {
            // 已过期或不存在：用默认过期时间
            redisUtils.set(statusVo.getKey(), JSONUtil.toJsonStr(qrCodeResult),60);
        }
        return ResultUtil.Succeed(qrCodeResult);
    }
}
