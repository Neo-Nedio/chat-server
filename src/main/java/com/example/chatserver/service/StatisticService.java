package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.NumInfoDto;
import com.example.chatserver.entity.Statistic;

import java.util.Date;

public interface StatisticService extends IService<Statistic> {
    void statisticLoginNum(Date yesterday);

    NumInfoDto numInfo();
}
