package com.example.chatserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
// Spring 框架提供的同步 HTTP 客户端工具，用于在 Java 代码中发送 HTTP 请求并接收响应。
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory); //创建一个带有自定义请求工厂的 RestTemplate
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(120 * 1000);      // 120秒
        factory.setConnectTimeout(120 * 1000);    // 120秒
        return factory;
    }
}
