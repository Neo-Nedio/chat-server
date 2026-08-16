package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.PhysicalFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface PhysicalFileMapper extends BaseMapper<PhysicalFile> {

    /**
     * 物理文件引用计数加1（数据库原子操作）
     */
    @Update("UPDATE `physical_file` SET `ref_count` = `ref_count` + 1, `update_time` = NOW(3) WHERE `id` = #{id}")
    int incRefCount(@Param("id") String id);

    /**
     * 物理文件引用计数减1（ref_count大于0才减，防止减为负数）
     */
    @Update("UPDATE `physical_file` SET `ref_count` = `ref_count` - 1, `update_time` = NOW(3) " +
            "WHERE `id` = #{id} AND `ref_count` > 0")
    int decRefCount(@Param("id") String id);

    /**
     * 查询引用计数为0且已持续1天的物理文件（定时清理用）。
     * update_time 随每次引用增减刷新，加1天条件是为了避开"上传后建逻辑文件前"等瞬时归零窗口
     */
    @Select("SELECT * FROM `physical_file` WHERE `ref_count` = 0 " +
            "AND `update_time` < DATE_SUB(NOW(3), INTERVAL 1 DAY)")
    List<PhysicalFile> listZeroRefBeforeOneDay();

    /**
     * 删除引用计数为0的物理文件（带ref_count=0守卫：删除瞬间若有秒传把计数加回去则不删，返回0）
     */
    @Delete("DELETE FROM `physical_file` WHERE `id` = #{id} AND `ref_count` = 0")
    int deleteZeroRefById(@Param("id") String id);
}
