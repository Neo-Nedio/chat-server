package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.TalkComment;
import com.example.chatserver.mapper.TalkCommentMapper;
import com.example.chatserver.service.TalkCommentService;
import org.springframework.stereotype.Service;


@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements TalkCommentService {

}
