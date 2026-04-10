package com.example.chatserver.scheduling;

import cn.hutool.core.date.DateUtil;
import com.example.chatserver.service.StatisticService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StatisticTasks {

    @Resource
    StatisticService statisticService;

    @Scheduled(cron = "0 2 0 * * ?")
    public void statisticLoginNum() {
        //定时任务，每天保存数据
        statisticService.statisticLoginNum(DateUtil.yesterday());
    }
}
