package com.example.chatserver.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


@Component
//在数据库操作时自动填充时间字段
public class MybatisHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), metaObject);
        this.setFieldValByName("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), metaObject);
    }

}
