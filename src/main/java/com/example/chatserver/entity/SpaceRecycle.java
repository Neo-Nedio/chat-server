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
@TableName("space_recycle")
public class SpaceRecycle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 所属用户id
     */
    @TableField("user_id")
    private String userId;

    /**
     * 空间id
     */
    @TableField("space_id")
    private String spaceId;

    /**
     * 文件id
     */
    @TableField("space_file_id")
    private String spaceFileId;

    /**
     * 过期时间，到期可彻底删除
     */
    @TableField("expire_at")
    private Date expireAt;

    /**
     * 入回收站时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 文件名称（联查space_file展示用）
     */
    @TableField(exist = false)
    private String fileName;

    /**
     * 文件分类（联查space_file展示用）
     */
    @TableField(exist = false)
    private String fileCategory;

    /**
     * 文件大小（联查space_file展示用）
     */
    @TableField(exist = false)
    private Long fileSize;
}
