package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.emoji;

import java.util.List;

public interface EmojiService extends IService<emoji> {

    List<emoji> list(String userId);

    boolean add(String userId,String emoji);

    boolean delete(String userId,String emoji);
}
