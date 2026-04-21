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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_group_member")
public class ChatGroupMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 聊天群id
     */
    @TableField("chat_group_id")
    private String chatGroupId;

    /**
     * 成员id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 群备注
     */
    @TableField("group_remark")
    private String groupRemark;

    /**
     * 群昵称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 上次群通知已读时间
     */
    @TableField("last_read_notice_time")
    private Date lastReadNoticeTime;

    /**
     * 禁言截止时间，NULL表示未禁言
     */
    @TableField("ban_end_time")
    private Date banEndTime;

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
