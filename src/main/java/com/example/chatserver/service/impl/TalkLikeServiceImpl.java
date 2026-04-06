package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.LikeListDto;
import com.example.chatserver.entity.Talk;
import com.example.chatserver.entity.TalkLike;
import com.example.chatserver.mapper.TalkLikeMapper;
import com.example.chatserver.service.TalkLikeService;
import com.example.chatserver.service.TalkService;
import com.example.chatserver.vo.talkLike.CreateTalkLikeVo;
import com.example.chatserver.vo.talkLike.DeleteTalkLikeVo;
import com.example.chatserver.vo.talkLike.TalkLikeListVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class TalkLikeServiceImpl extends ServiceImpl<TalkLikeMapper, TalkLike> implements TalkLikeService {

    @Resource
    TalkLikeMapper talkLikeList;

    @Resource
    TalkService talkService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createTalkLike(String userId, CreateTalkLikeVo createTalkLikeVo) {
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, createTalkLikeVo.getTalkId())
                .eq(TalkLike::getUserId, userId);
        if (count(queryWrapper) <= 0) {

            Talk talk = talkService.getById(createTalkLikeVo.getTalkId());
            talk.setLikeNum(talk.getLikeNum() + 1);
            talkService.updateById(talk);

            TalkLike talkLike = new TalkLike();
            talkLike.setId(IdUtil.randomUUID());
            talkLike.setTalkId(createTalkLikeVo.getTalkId());
            talkLike.setUserId(userId);
            return save(talkLike);
        }
        return false;
    }

    @Override
    public List<LikeListDto> talkLikeList(String userId, TalkLikeListVo talkLikeListVo) {
        return talkLikeList.talkLikeList(userId, talkLikeListVo.getTalkId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkLike(String userId, DeleteTalkLikeVo deleteTalkLikeVo) {
        Talk talk = talkService.getById(deleteTalkLikeVo.getTalkId());
        talk.setLikeNum(talk.getLikeNum() - 1);
        talkService.updateById(talk);
        LambdaQueryWrapper<TalkLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TalkLike::getTalkId, deleteTalkLikeVo.getTalkId())
                .eq(TalkLike::getUserId, userId);
        return remove(queryWrapper);
    }
}
