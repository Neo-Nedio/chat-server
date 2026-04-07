package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.ChatListDto;
import com.example.chatserver.entity.ChatList;
import com.example.chatserver.service.ChatListService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.chatlist.CreateChatListVo;
import com.example.chatserver.vo.chatlist.DeleteChatListVo;
import com.example.chatserver.vo.chatlist.TopChatListVo;
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
        ChatList result = chatListService.createChatList(userId, createChatListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    public JSONObject deleteChatList(@Userid String userId, @RequestBody DeleteChatListVo deleteChatListVo) {
        boolean result = chatListService.deleteChatList(userId, deleteChatListVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 设置置顶会话
     */
    @PostMapping("/top")
    public JSONObject topChatList(@Userid String userId, @RequestBody TopChatListVo topChatListVo) {
        boolean result = chatListService.topChatList(userId, topChatListVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 消息已读
     */
    @GetMapping("/read/{targetId}")
    public JSONObject messageRead(@Userid String userId, @PathVariable String targetId) {
        boolean result = chatListService.messageRead(userId, targetId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 全部已读
     */
    @GetMapping("/read/all")
    public JSONObject messageReadAll(@Userid String userId) {
        boolean result = chatListService.messageReadAll(userId);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 获取详细信息
     */
    @GetMapping("/detail/{targetId}")
    public JSONObject detailChartList(@Userid String userId, @PathVariable String targetId) {
        ChatList result = chatListService.detailChatList(userId, targetId);
        return ResultUtil.Succeed(result);
    }
}

