package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.CommentListDto;
import com.example.chatserver.entity.Talk;
import com.example.chatserver.entity.TalkComment;
import com.example.chatserver.mapper.TalkCommentMapper;
import com.example.chatserver.service.TalkCommentService;
import com.example.chatserver.service.TalkService;
import com.example.chatserver.vo.talkComment.CreateTalkCommentVo;
import com.example.chatserver.vo.talkComment.DeleteTalkCommentVo;
import com.example.chatserver.vo.talkComment.TalkCommentListVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements TalkCommentService {

    @Resource
    TalkCommentMapper talkCommentMapper;

    @Resource
    TalkService talkService;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createTalkComment(String userId, CreateTalkCommentVo createTalkCommentVo) {

        Talk talk = talkService.getById(createTalkCommentVo.getTalkId());
        talk.setCommentNum(talk.getCommentNum() + 1);
        talkService.updateById(talk);

        TalkComment talkComment = new TalkComment();
        talkComment.setId(IdUtil.randomUUID());
        talkComment.setTalkId(createTalkCommentVo.getTalkId());
        talkComment.setUserId(userId);
        talkComment.setContent(createTalkCommentVo.getComment());

        return save(talkComment);
    }

    @Override
    public List<CommentListDto> talkCommentList(String userId, TalkCommentListVo talkCommentListVo) {
        return talkCommentMapper.talkCommentList(userId, talkCommentListVo.getTalkId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteTalkComment(String userId, DeleteTalkCommentVo deleteTalkLikeVo) {
        Talk talk = talkService.getById(deleteTalkLikeVo.getTalkId());
        talk.setCommentNum(talk.getCommentNum() - 1);
        talkService.updateById(talk);
        return removeById(deleteTalkLikeVo.getTalkCommentId());
    }
}
