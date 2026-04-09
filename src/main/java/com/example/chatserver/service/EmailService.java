package com.example.chatserver.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
@Service
public class EmailService {
    @Resource
    private JavaMailSender mailSender;  // 邮件发送器

    @Resource
    private TemplateEngine templateEngine;  // Thymeleaf 模板引擎

    @Value("${spring.mail.username}")
    String from;

    public void sendHtmlMessage(String to, String subject, String templateName, Context context) throws MessagingException {
        //渲染模板
        String process = templateEngine.process(templateName, context);

        //创建邮件
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        //设置邮件内容
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(process, true); // true 表示 HTML 格式

        //发送邮件
        mailSender.send(message);
    }
}
