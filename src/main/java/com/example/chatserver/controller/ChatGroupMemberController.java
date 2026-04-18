package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.ChatListMember.MemberListVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/chat-group-member")
public class ChatGroupMemberController {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

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

    @GetMapping("isMember")
    public JSONObject isMember(@Userid String userId,@RequestParam("groupId") String groupId) {
        boolean result = chatGroupMemberService.isMemberExists(groupId,userId);
        return ResultUtil.ResultByFlag(result);
    }
}
