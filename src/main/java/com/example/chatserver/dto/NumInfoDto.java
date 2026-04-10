package com.example.chatserver.dto;

import com.example.chatserver.entity.Statistic;
import lombok.Data;

import java.util.List;

@Data
public class NumInfoDto {
    private Integer loginNum;
    private Integer onlineNum;
    private Integer msgNum;
    private List<Statistic> statistics;
}
