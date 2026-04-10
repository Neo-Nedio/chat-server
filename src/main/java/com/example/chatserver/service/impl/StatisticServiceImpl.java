package com.example.chatserver.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.NumInfoDto;
import com.example.chatserver.dto.Top10MsgDto;
import com.example.chatserver.entity.Statistic;
import com.example.chatserver.mapper.StatisticMapper;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.service.StatisticService;
import com.example.chatserver.service.UserOperatedService;
import com.example.chatserver.websocket.WebSocketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class StatisticServiceImpl extends ServiceImpl<StatisticMapper, Statistic> implements StatisticService {

    @Resource
    UserOperatedService userOperatedService;

    @Resource
    StatisticMapper statisticMapper;

    @Resource
    WebSocketService webSocketService;

    @Resource
    MessageService messageService;

    @Override
    //定时任务管理，保存登录数据
    public void statisticLoginNum(Date yesterday) {
        Integer loginNum = userOperatedService.uniqueLoginNum(yesterday);
        LambdaQueryWrapper<Statistic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Statistic::getDate, yesterday);
        Statistic statistic = getOne(queryWrapper);
        if (null == statistic) {
            statistic = new Statistic();
            statistic.setId(IdUtil.randomUUID());
        }
        statistic.setDate(yesterday);
        statistic.setLoginNum(loginNum);
        saveOrUpdate(statistic);
    }

    @Override
    public NumInfoDto numInfo() {
        DateTime date = DateUtil.parseDate(DateUtil.today());
        List<Statistic> statisticList = getStatisticList(7); //获取前七天的数据

        NumInfoDto numInfoDto = new NumInfoDto();
        numInfoDto.setStatistics(statisticList);
        //获取今天的数据(除登录数据外还有在线数量，消息数量)
        numInfoDto.setLoginNum(userOperatedService.uniqueLoginNum(date));
        numInfoDto.setMsgNum(messageService.messageNum(date));
        numInfoDto.setOnlineNum(webSocketService.getOnlineNum());
        return numInfoDto;
    }

    public List<Statistic> getStatisticList(int day) {
        return statisticMapper.getStatisticList(day);
    }

    @Override
    public List<Top10MsgDto> top10Msg() {
        return messageService.getTop10Msg(new Date());
    }
}
