package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.SetsDto;
import com.example.chatserver.entity.UserSet;
import com.example.chatserver.mapper.UserSetMapper;
import com.example.chatserver.service.UserSetService;
import com.example.chatserver.vo.userSet.UpdateUserSetVo;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

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

        @Override
    public boolean updateUserSet(String userId, UpdateUserSetVo updateUserSetVo) {
        LambdaQueryWrapper<UserSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSet::getUserId, userId);
        UserSet userSet = getOne(queryWrapper);
        if (userSet == null) {
            return false;
        }
        SetsDto sets = userSet.getSets();
        try {
            //  通过反射获取字段
            Field field = SetsDto.class.getDeclaredField(updateUserSetVo.getKey());
            field.setAccessible(true);  // 访问私有字段
            field.set(sets, updateUserSetVo.getValue());  // 设置新值
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
        return updateById(userSet);
    }
}
