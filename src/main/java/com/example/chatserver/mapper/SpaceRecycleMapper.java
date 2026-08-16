package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.SpaceRecycle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface SpaceRecycleMapper extends BaseMapper<SpaceRecycle> {

    /**
     * 查询用户回收站列表（联查space_file补充展示字段，过期记录不展示）
     */
    @Select("SELECT r.*, f.`file_name` AS file_name, f.`file_category` AS file_category, f.`file_size` AS file_size " +
            "FROM `space_recycle` AS r " +
            "LEFT JOIN `space_file` AS f ON f.`id` = r.`space_file_id` " +
            "WHERE r.`user_id` = #{userId} AND r.`space_id` = #{spaceId} " +
            "AND r.`expire_at` IS NOT NULL AND r.`expire_at` >= NOW(3) " +
            "ORDER BY r.`create_time` DESC")
    List<SpaceRecycle> listByUserIdAndSpaceId(@Param("userId") String userId, @Param("spaceId") String spaceId);

    /**
     * 按id批量查询（过期记录不可还原）
     */
    @Select("<script>" +
            "SELECT * FROM `space_recycle` WHERE `user_id` = #{userId} AND `space_id` = #{spaceId} " +
            "AND `expire_at` IS NOT NULL AND `expire_at` >= NOW(3) AND `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<SpaceRecycle> listValidByIds(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("spaceId") String spaceId);

    /**
     * 按id批量查询（忽略过期时间，彻底删除/清空时使用）
     */
    @Select("<script>" +
            "SELECT * FROM `space_recycle` WHERE `user_id` = #{userId} AND `space_id` = #{spaceId} AND `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<SpaceRecycle> listByIdsIgnoreExpire(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("spaceId") String spaceId);

    /**
     * 查询用户回收站全部记录id（清空回收站时使用）
     */
    @Select("SELECT `id` FROM `space_recycle` WHERE `user_id` = #{userId} AND `space_id` = #{spaceId}")
    List<String> listIdsByUserIdAndSpaceId(@Param("userId") String userId, @Param("spaceId") String spaceId);

    /**
     * 查询已过期的回收站记录（定时清理用）
     */
    @Select("SELECT * FROM `space_recycle` WHERE `expire_at` IS NOT NULL AND `expire_at` < NOW(3)")
    List<SpaceRecycle> listExpired();

    /**
     * 按id批量物理删除回收站记录
     */
    @Delete("<script>" +
            "DELETE FROM `space_recycle` WHERE `id` IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int permanentDeleteByIds(@Param("ids") List<String> ids);
}
