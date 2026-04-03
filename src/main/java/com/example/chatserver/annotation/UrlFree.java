package com.example.chatserver.annotation;

import java.lang.annotation.*;

@Documented //生成 API 文档时包含该注解信息
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留（可通过反射读取）
@Target({ElementType.METHOD, ElementType.TYPE})  // 可用在方法和类上
public @interface UrlFree {
    String value() default "";
}
