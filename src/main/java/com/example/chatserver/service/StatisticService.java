package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.NumInfoDto;
import com.example.chatserver.dto.Top10MsgDto;
import com.example.chatserver.entity.Statistic;

import java.util.Date;
import java.util.List;

public interface StatisticService extends IService<Statistic> {
    void statisticLoginNum(Date yesterday);

    NumInfoDto numInfo();

    List<Top10MsgDto> top10Msg();
}
