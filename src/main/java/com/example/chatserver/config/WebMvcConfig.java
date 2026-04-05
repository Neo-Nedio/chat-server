package com.example.chatserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
//Spring MVC 配置类，用于自定义控制器（Controller）的方法参数解析器
//注册一个自定义的 UserInfoArgumentResolver，让控制器方法能直接通过参数获取当前登录用户信息，无需手动从 Session/Token 中提取。
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) { //Spring 维护的解析器列表
        resolvers.add(new UserInfoArgumentResolver()); //将自定义解析器添加到列表中
    }
}
