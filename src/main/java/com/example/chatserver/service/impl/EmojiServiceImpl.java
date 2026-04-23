package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.emoji;
import com.example.chatserver.mapper.EmojiMapper;
import com.example.chatserver.service.EmojiService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EmojiServiceImpl extends ServiceImpl<EmojiMapper, emoji> implements EmojiService {

    @Resource
    EmojiMapper emojiMapper;

    @Override
    public List<emoji> list(String userId) {
        LambdaQueryWrapper<emoji> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(emoji::getUserId, userId)
                .orderByDesc(emoji::getCreateTime);;
        return emojiMapper.selectList(queryWrapper);
    }
}
