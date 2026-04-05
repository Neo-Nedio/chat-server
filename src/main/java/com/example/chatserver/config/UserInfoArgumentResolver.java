package com.example.chatserver.config;



import com.example.chatserver.annotation.UserInfo;
import com.example.chatserver.annotation.Userid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

//当控制器方法参数带有 @UserInfo 或 @Userid 注解时，自动从 request 的 userinfo 属性中获取用户信息，无需手动解析
public class UserInfoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    //判断是否支持该参数
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserInfo.class) ||
                parameter.hasParameterAnnotation(Userid.class);
    }

    //实际解析参数并返回值
    @Override
    public Object resolveArgument(
            MethodParameter parameter, //当前方法参数的信息（类型、注解等）
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, //封装了请求信息的对象
            @Nullable WebDataBinderFactory binderFactory) {

        //获取原始请求对象
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        if (parameter.hasParameterAnnotation(UserInfo.class)) {
            return request.getAttribute("userinfo"); //直接从 request 中获取名为 "userinfo" 的属性
        } else if (parameter.hasParameterAnnotation(Userid.class)) {
            Map<String, Object> userinfo = (Map<String, Object>) request.getAttribute("userinfo");
            if (userinfo != null) {
                return userinfo.get("userId"); //返回用户id
            }
        }
        return null;
    }
}
