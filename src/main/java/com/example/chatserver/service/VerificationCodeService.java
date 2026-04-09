package com.example.chatserver.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.chatserver.utils.RedisUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@Service
@Slf4j
public class VerificationCodeService {

    @Resource
    EmailService emailService;

    @Resource
    RedisUtils redisUtils;

    //发送验证码
    public void emailVerificationCode(String email) {
        //验证码还在就返回
        String code = (String) redisUtils.get(email);
        if (code != null) {
            return;
        }

        code = RandomUtil.randomNumbers(6); //生成验证码
        redisUtils.set(email, code, 10); //10分钟后自动失效

        //准备模板数据
        Context context = new Context();
        context.setVariable("nowDate", DateUtil.now());
        context.setVariable("code", code.toCharArray());
        try {
            //发送邮件
            emailService.sendHtmlMessage(email, "验证码", "email_template.html", context);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }
}
