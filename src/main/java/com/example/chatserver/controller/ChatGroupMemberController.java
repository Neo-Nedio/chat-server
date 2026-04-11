package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.ChatListMember.MemberListVo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/chat-group-member")
public class ChatGroupMemberController {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    MinioUtil minioUtil;

    @PostMapping("/list")
    public JSONObject memberList(@Userid String userId, @RequestBody MemberListVo memberListVo) {
        Map<String, MemberListDto> result = chatGroupMemberService.memberList(userId, memberListVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/list/page")
    public JSONObject memberListPage(@Userid String userId, @RequestBody MemberListVo memberListVo) {
        List<MemberListDto> result = chatGroupMemberService.memberListPage(userId, memberListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 设置聊天背景
     */
    @PostMapping("/set-chat-background")
    public JSONObject setChatBackground(@Userid String userId,
                                        @RequestParam("groupId") String groupId,
                                        @RequestParam("name") String name,
                                        @RequestParam("type") String type,
                                        @RequestParam("size") long size,
                                        @RequestParam("file") MultipartFile file) {

        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, groupId);
        ChatGroupMember chatGroupMember = chatGroupMemberService.getOne(queryWrapper);

        if (chatGroupMember == null) return ResultUtil.Fail("群聊不存在");
        boolean update;
        String url;
        try {
            String fileName = userId+"-"+groupId + "-chat-background" + name.substring(name.lastIndexOf("."));
            url = minioUtil.upload(file.getInputStream(), fileName, type, size);
            url += "?t=" + System.currentTimeMillis();
            chatGroupMember.setChatBackground(url);
            update = chatGroupMemberService.updateById(chatGroupMember);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!update) return ResultUtil.Fail("设置失败");
        return ResultUtil.Succeed(url);
    }
}
