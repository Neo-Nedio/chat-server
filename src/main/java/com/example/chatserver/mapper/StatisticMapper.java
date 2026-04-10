package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.Statistic;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatisticMapper extends BaseMapper<Statistic> {

    @Select("SELECT * " +
            "FROM `statistic` " +
            "WHERE `date` >= DATE_SUB(CURDATE(), INTERVAL #{day} DAY) " +
            "  AND `date` <= CURDATE() " +
            "ORDER BY `date` ASC ")
    //查询最近 N 天统计数据
    List<Statistic> getStatisticList(int day);
}
