package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.chatGroup.CreateChatGroupVo;
import com.example.chatserver.vo.chatGroup.DetailsChatGroupVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/v1/api/chat-group")
public class ChatGroupController {

    @Resource
    ChatGroupService chatGroupService;

    /**
     * 聊天群列表
     */
    @GetMapping("/list")
    public JSONObject chatGroupList(@Userid String userId) {
        List<ChatGroup> result = chatGroupService.chatGroupList(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 创建聊天群
     */
    @PostMapping("/create")
    public JSONObject createChatGroup(@Userid String userId, @RequestBody CreateChatGroupVo createChatGroupVo) {
        boolean result = chatGroupService.createChatGroup(userId, createChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 群详情
     */
    @PostMapping("/details")
    public JSONObject detailsChatGroup(@Userid String userId, @RequestBody DetailsChatGroupVo detailsChatGroupVo) {
        ChatGroupDetailsDto result = chatGroupService.detailsChatGroup(userId, detailsChatGroupVo);
        return ResultUtil.Succeed(result);
    }
}
