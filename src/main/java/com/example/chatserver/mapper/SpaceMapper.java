package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.Space;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface SpaceMapper extends BaseMapper<Space> {

    /**
     * 累加空间已用容量与文件数（数据库原子操作，直接在数据库改，而不是查出来改，防止并发超卖）
     */
    @Update("UPDATE `space` SET `used_bytes` = `used_bytes` + #{size}, `file_count` = `file_count` + #{count}, " +
            "`update_time` = NOW(3) WHERE `id` = #{id}")
    int incUsedBytes(@Param("id") String id, @Param("size") long size, @Param("count") long count);

    /**
     * 扣减空间已用容量与文件数（GREATEST兜底，防止减为负数）
     */
    @Update("UPDATE `space` SET `used_bytes` = GREATEST(`used_bytes` - #{size}, 0), `file_count` = GREATEST(`file_count` - #{count}, 0), " +
            "`update_time` = NOW(3) WHERE `id` = #{id}")
    int decUsedBytes(@Param("id") String id, @Param("size") long size, @Param("count") long count);
}
