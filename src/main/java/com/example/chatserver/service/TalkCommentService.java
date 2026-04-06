package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.CommentListDto;
import com.example.chatserver.entity.TalkComment;
import com.example.chatserver.vo.talkComment.CreateTalkCommentVo;
import com.example.chatserver.vo.talkComment.DeleteTalkCommentVo;
import com.example.chatserver.vo.talkComment.TalkCommentListVo;

import java.util.List;

public interface TalkCommentService extends IService<TalkComment> {
    boolean createTalkComment(String userId, CreateTalkCommentVo createTalkCommentVo);

    List<CommentListDto> talkCommentList(String userId, TalkCommentListVo talkCommentListVo);

    boolean deleteTalkComment(String userId, DeleteTalkCommentVo deleteTalkLikeVo);
}
