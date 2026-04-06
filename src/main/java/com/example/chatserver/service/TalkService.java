package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.TalkListDto;
import com.example.chatserver.entity.Talk;
import com.example.chatserver.vo.talk.CreateTalkVo;
import com.example.chatserver.vo.talk.DeleteTalkVo;
import com.example.chatserver.vo.talk.TalkListVo;

import java.util.List;


public interface TalkService extends IService<Talk> {

    List<TalkListDto> talkList(String userId, TalkListVo talkListVo);

    Talk createTalk(String userId, CreateTalkVo createTalkVo);

    Talk updateTalkImg(String userId, String talkId, String imgName);

    boolean deleteTalk(String userId, DeleteTalkVo deleteTalkVo);
}
