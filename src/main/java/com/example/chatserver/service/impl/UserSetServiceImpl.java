package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.SetsDto;
import com.example.chatserver.entity.UserSet;
import com.example.chatserver.mapper.UserSetMapper;
import com.example.chatserver.service.UserSetService;
import org.springframework.stereotype.Service;

@Service
public class UserSetServiceImpl extends ServiceImpl<UserSetMapper, UserSet> implements UserSetService {

    @Override
    public UserSet getUserSet(String userId) {
        LambdaQueryWrapper<UserSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSet::getUserId, userId);
        UserSet userSet = getOne(queryWrapper);
        if (null == userSet) {
            userSet = new UserSet();
            userSet.setId(IdUtil.randomUUID());
            userSet.setUserId(userId);
            userSet.setSets(SetsDto.defaultSets());
            save(userSet);
        }
        return userSet;
    }
}
