package com.example.chatserver.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER) // 该注解只能用在方法参数上
@Retention(RetentionPolicy.RUNTIME)
public @interface UserInfo {
}
