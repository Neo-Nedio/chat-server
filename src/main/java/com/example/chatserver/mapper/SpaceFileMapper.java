package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.SpaceCategoryStatDto;
import com.example.chatserver.entity.SpaceFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface SpaceFileMapper extends BaseMapper<SpaceFile> {

    /**
     * 按id批量查询（含回收站中的记录，还原/彻底删除时使用）
     */
    @Select("<script>" +
            "SELECT * FROM `space_file` WHERE `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<SpaceFile> listByIdsAll(@Param("ids") List<String> ids);

    /**
     * 回收站还原：清除逻辑删除标记（原生SQL绕过@TableLogic）
     */
    @Update("<script>" +
            "UPDATE `space_file` SET `deleted` = 0, `update_time` = NOW(3) WHERE `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int restoreByIds(@Param("ids") List<String> ids);

    /**
     * 按id批量物理删除（彻底删除，绕过@TableLogic）
     */
    @Delete("<script>" +
            "DELETE FROM `space_file` WHERE `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int permanentDeleteByIds(@Param("ids") List<String> ids);

    /**
     * 按文件分类统计数量与大小
     */
    @Select("SELECT `file_category` AS fileCategory, COUNT(*) AS fileCount, COALESCE(SUM(`file_size`), 0) AS totalSize " +
            "FROM `space_file` WHERE `space_id` = #{spaceId} " +
            "GROUP BY `file_category`")
    List<SpaceCategoryStatDto> statByCategory(@Param("spaceId") String spaceId);
}
