package com.example.chatserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("friend")
public class Friend implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 好友id
     */
    @TableField("friend_id")
    private String friendId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 分组id
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 是否拉黑
     */
    @TableField("is_back")
    private Boolean isBack;

    /**
     * 是否特别关心
     */
    @TableField("is_concern")
    private Boolean isConcern;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String name;
}
