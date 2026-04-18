package com.example.chatserver.service;

import com.example.chatserver.dto.EmailTaskDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
@Service
@Slf4j
public class EmailService {
    @Resource
    private JavaMailSender mailSender;  // 邮件发送器

    @Resource
    private TemplateEngine templateEngine;  // Thymeleaf 模板引擎

    @Resource
    private MQProducerService mqProducerService;

    @Value("${spring.mail.username}")
    String from;

    public void sendHtmlMessage(String to, String subject, String templateName, Context context) throws MessagingException {
        try {
            //渲染模板
            String html = templateEngine.process(templateName, context);
            EmailTaskDto task = new EmailTaskDto(to, subject, html);

            //投递到 MQ 异步发送，未启用 MQ 时回退到同步发送
            if (!mqProducerService.sendEmail(task)) {
                doSend(task);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 真正调用 SMTP 发送邮件，由 MQ 消费者回调，或在 MQ 未启用时直接调用
     */
    public void doSend(EmailTaskDto task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            //设置邮件内容
            helper.setFrom(from);
            helper.setTo(task.getTo());
            helper.setSubject(task.getSubject());
            helper.setText(task.getHtml(), true); // true 表示 HTML 格式

            //发送邮件
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败 to={}, subject={}, err={}", task.getTo(), task.getSubject(), e.getMessage());
        }
    }
}
