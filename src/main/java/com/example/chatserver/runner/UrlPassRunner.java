package com.example.chatserver.runner;

import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.utils.UrlPermitUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//启动时自动扫描并加载免验证 URL 的运行器，会在 Spring 启动完成后自动执行。
@Component
public class UrlPassRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(UrlPassRunner.class);

    @Resource
    private UrlPermitUtil urlPermitUtil;  // 免验证 URL 工具类

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void run(ApplicationArguments args) {
        //获取所有 Controller 映射
        Map<RequestMappingInfo, HandlerMethod> methodMap = requestMappingHandlerMapping.getHandlerMethods();

        List<String> urlList = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : methodMap.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();   // 获取映射信息
            HandlerMethod handlerMethod = entry.getValue();            // 获取方法对象
            Annotation[] annotations = handlerMethod.getMethod().getAnnotations(); //获取方法上的所有注解
            for (Annotation annotation : annotations) {
                // 免验证url
                if (annotation.annotationType().equals(UrlFree.class)) {
                    //获取请求路径
                    Set<String> directPaths = requestMappingInfo.getPatternValues();
                    for (String url : directPaths) {
                        //提取路径并转换通配符  将 {id} 替换为 **
                        urlList.add(url.replaceAll("\\{[^\\}]+\\}", "**"));
                    }
                }
            }
        }
        //将所有收集到的免验证 URL 添加到 UrlPermitUtil 中，供过滤器使用。
        urlPermitUtil.addUrls(urlList);
        logger.info("-----not verify that the url is successfully loaded-----");
    }
}
