package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.chatlist.CreateChatListVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/api/chat-list")
public class ChatListController {
    @Resource
    ChatListService chatListService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/list")
    public JSONObject getChatList(@Userid String userId) {
        ChatListDto chatList = chatListService.getChatList(userId);
        return ResultUtil.Succeed(chatList);
    }

    /**
     * 创建聊天会话
     */
    @PostMapping("/create")
    public JSONObject createChatList(@Userid String userId, @RequestBody CreateChatListVo createChatListVo) {
        boolean result = chatListService.createChatList(userId, createChatListVo);
        return ResultUtil.ResultByFlag(result);
    }
}

