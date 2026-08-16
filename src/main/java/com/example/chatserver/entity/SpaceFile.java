package com.example.chatserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
@TableName("space_file")
public class SpaceFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    /**
     * 空间id
     */
    @TableField("space_id")
    private String spaceId;

    /**
     * 物理文件id
     */
    @TableField("physical_id")
    private String physicalId;

    /**
     * 文件名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件分类：image/video/document/audio/archive
     */
    @TableField("file_category")
    private String fileCategory;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 逻辑删除：0-正常，1-已删除（回收站中）
     */
    @TableLogic //MyBatis-Plus 提供的“逻辑删除”注解，让删除操作变成“更新标记位”，而不是真的从数据库里把数据删掉
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 上传时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
