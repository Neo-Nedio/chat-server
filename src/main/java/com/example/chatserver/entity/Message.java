package com.example.chatserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.chatserver.entity.ext.MsgContent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false) //只比较当前类的字段，不比较父类的字段
@Accessors(chain = true) //让 Lombok 生成的 setter 方法返回 this（当前对象），实现链式调用。
@TableName(value = "message", autoResultMap = true)
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 消息发送方id
     */
    @TableField("from_id")
    private String fromId;

    /**
     * 消息接受方id
     */
    @TableField("to_id")
    private String toId;

    /**
     * 消息类型
     * 消息大类（给谁发：用户消息还是群消息，和 MQ/推送路由相关）
     */
    @TableField("`type`") //反引号告诉 MySQL 这是列名
    private String type;

    /**
     * 消息内容
     */
    //typeHandler = JacksonTypeHandler.class 用于处理 JSON 类型字段 的类型处理器，实现 Java 对象与数据库 JSON 字段之间的自动转换。
    @TableField(value = "msg_content", typeHandler = JacksonTypeHandler.class)
    private MsgContent msgContent;

    /**
     * 是否显示时间
     */
    @TableField("is_show_time")
    private Boolean isShowTime;

    /**
     * 消息状态
     */
    @TableField("status")
    private String status;

    /**
     * 消息源
     * source：消息来源/会话维度（私聊还是群聊）
     */
    @TableField("source")
    private String source;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    //格式化日期时间在序列化（Java → JSON）和反序列化（JSON → Java）时的格式。
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
