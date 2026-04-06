package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.TalkLike;
import com.example.chatserver.mapper.TalkLikeMapper;
import com.example.chatserver.service.TalkLikeService;
import org.springframework.stereotype.Service;


@Service
public class TalkLikeServiceImpl extends ServiceImpl<TalkLikeMapper, TalkLike> implements TalkLikeService {

}
