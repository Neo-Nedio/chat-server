package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.admin.vo.statistic.LoginDetailsVo;
import com.example.chatserver.constant.UserOperatedType;
import com.example.chatserver.dto.UserOperatedDto;
import com.example.chatserver.entity.UserOperated;
import com.example.chatserver.mapper.UserOperatedMapper;
import com.example.chatserver.service.UserOperatedService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserOperatedServiceImpl extends ServiceImpl<UserOperatedMapper, UserOperated> implements UserOperatedService {

    @Resource
    UserOperatedMapper userOperatedMapper;

    @Override
    public boolean recordLogin(String id, String ip) {
        UserOperated operated = new UserOperated();
        operated.setId(IdUtil.randomUUID());
        operated.setUserId(id);
        operated.setContent(ip);
        operated.setType(UserOperatedType.Login);
        return save(operated);
    }

    @Override
    public List<UserOperatedDto> loginDetails(LoginDetailsVo loginDetailsVo) {
        return userOperatedMapper.loginDetails(loginDetailsVo.getIndex(), loginDetailsVo.getNum(), loginDetailsVo.getKeyword());
    }

    @Override
    public Integer uniqueLoginNum(Date date) {
        return userOperatedMapper.uniqueLoginNum(date);
    }
}
