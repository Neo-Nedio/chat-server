package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.LikeListDto;
import com.example.chatserver.entity.TalkLike;
import com.example.chatserver.vo.talkLike.CreateTalkLikeVo;
import com.example.chatserver.vo.talkLike.DeleteTalkLikeVo;
import com.example.chatserver.vo.talkLike.TalkLikeListVo;

import java.util.List;

public interface TalkLikeService extends IService<TalkLike> {
    boolean createTalkLike(String userId, CreateTalkLikeVo createTalkLikeVo);

    List<LikeListDto> talkLikeList(String userId, TalkLikeListVo talkLikeListVo);

    boolean deleteTalkLike(String userId, DeleteTalkLikeVo deleteTalkLikeVo);
}
