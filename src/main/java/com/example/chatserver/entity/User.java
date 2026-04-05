package com.example.chatserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false) //只比较当前类的字段，不比较父类的字段
@Accessors(chain = true) //让 Lombok 生成的 setter 方法返回 this（当前对象），实现链式调用。
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 用户账号
     */
    @TableField("account")
    private String account;

    /**
     * 用户名
     */
    @TableField("name")
    private String name;

    /**
     * 头像
     */
    @TableField("portrait")
    private String portrait;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 性别
     */
    @TableField("sex")
    private String sex;

    /**
     * 生日
     */
    @TableField("birthday")
    private Date birthday;

    /**
     * 签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 最后操作时间
     */
    //格式化日期时间在序列化（Java → JSON）和反序列化（JSON → Java）时的格式。
    @TableField("last_opt_time")
    private Date lastOptTime;

    /**
     * 用户状态
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
