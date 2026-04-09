package com.example.chatserver.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
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

    @Bean
    //跨域请求过滤器的配置
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        //创建 CORS 配置源，用于管理不同路径的 CORS 规则
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        //允许携带认证信息（如 Cookie、Authorization 头）
        config.setAllowCredentials(true);
        //允许所有来源的请求访问。
        config.addAllowedOriginPattern("*");
        //允许所有请求头
        config.addAllowedHeader("*");
        //允许所有 HTTP 方法
        config.addAllowedMethod("*");
        //将 CORS 配置应用到所有路径（/** 表示所有路径）
        source.registerCorsConfiguration("/**", config);
        //创建过滤器注册 Bean，并设置优先级为 0（数值越小优先级越高
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
}
