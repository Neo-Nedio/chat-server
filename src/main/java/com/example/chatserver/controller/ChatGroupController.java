package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.chatGroup.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/api/chat-group")
public class ChatGroupController {

    @Resource
    ChatGroupService chatGroupService;

    @Resource
    MinioUtil minioUtil;


    /**
     * 搜索聊天群
     */
    @GetMapping("/search")
    public JSONObject searchGroup(@Userid String userId, @RequestParam("search") String search) {
        List<ChatGroup> result = chatGroupService.searchGroup(userId,search);
        return ResultUtil.Succeed(result);
    }

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
    public JSONObject createChatGroup(@Userid String userId, @Valid @RequestBody CreateChatGroupVo createChatGroupVo) {
        boolean result = chatGroupService.createChatGroup(userId, createChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 更新群信息(个人)
     */
    @PostMapping("/update")
    public JSONObject updateChatGroup(@Userid String userId, @Valid @RequestBody UpdateChatGroupVo updateChatGroupVo) {
        boolean result = chatGroupService.updateChatGroup(userId, updateChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 更新群信息(群名称)
     */
    @PostMapping("/update/name")
    public JSONObject updateChatGroupName(@Userid String userId, @Valid @RequestBody UpdateChatGroupNameVo updateChatGroupNameVo) {
        boolean result = chatGroupService.updateChatGroupName(userId, updateChatGroupNameVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 成员邀请
     */
    @PostMapping("/invite")
    public JSONObject inviteMember(@Userid String userId, @Valid @RequestBody InviteMemberVo inviteMemberVo) {
        boolean result = chatGroupService.inviteMember(userId, inviteMemberVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 退出群聊
     */
    @PostMapping("/quit")
    public JSONObject quitChatGroup(@Userid String userId, @Valid @RequestBody QuitChatGroupVo quitChatGroupVo) {
        boolean result = chatGroupService.quitChatGroup(userId, quitChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 踢出群聊
     */
    @PostMapping("/kick")
    public JSONObject kickChatGroup(@Userid String userId, @Valid @RequestBody KickChatGroupVo kickChatGroupVo) {
        boolean result = chatGroupService.kickChatGroup(userId, kickChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 解散群聊
     */
    @PostMapping("/dissolve")
    public JSONObject dissolveChatGroup(@Userid String userId, @Valid @RequestBody DissolveChatGroupVo dissolveChatGroupVo) {
        boolean result = chatGroupService.dissolveChatGroup(userId, dissolveChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 查看群聊是否解散
     */
    @PostMapping("/isDissolve")
    public JSONObject isDissolveChatGroup(@Userid String userId, @Valid @RequestBody DissolveChatGroupVo dissolveChatGroupVo) {
        boolean result = chatGroupService.isDissolveChatGroup( dissolveChatGroupVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 转让群聊
     */
    @PostMapping("/transfer")
    public JSONObject transferChatGroup(@Userid String userId, @Valid @RequestBody TransferChatGroupVo transferChatGroupVo) {
        boolean result = chatGroupService.transferChatGroup(userId, transferChatGroupVo);
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

    /**
     * 更新群头像
     */
    @PostMapping(value = "/upload/portrait")
    public JSONObject upload(HttpServletRequest request,
                             @Userid String userId,
                             @RequestHeader("groupId") String groupId,
                             @RequestHeader("name") String name,
                             @RequestHeader("type") String type,
                             @RequestHeader("size") long size) throws IOException {
        ChatGroup chatGroup= chatGroupService.getById(groupId);
        if (chatGroup == null) {
            return ResultUtil.Fail("群聊不存在");
        }
        if(!chatGroup.getOwnerUserId().equals(userId)) {
            throw new BaseException("您不是群主~");
        }

        if(StringUtils.isNotBlank(chatGroup.getPortrait())){
            minioUtil.remove(chatGroup.getPortrait());
        }

        //添加缓存破坏参数 防止浏览器缓存旧头像。每次更新头像后，URL 都会不同，强制刷新。
        String fileName = groupId + "-portrait" +  System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        minioUtil.upload(request.getInputStream(), fileName, type, size);
        chatGroupService.updateGroupPortrait(groupId, fileName);
        return ResultUtil.Succeed(fileName);
    }

    /**
     * 更新群头像（表单）
     */
    @PostMapping(value = "/upload/portrait/form")
    public JSONObject uploadForm(MultipartFile file,
                                 @Userid String userId,
                                 @RequestParam("groupId") String groupId,
                                 @RequestParam("name") String name,
                                 @RequestParam("type") String type,
                                 @RequestParam("size") long size) throws IOException {
        ChatGroup chatGroup= chatGroupService.getById(groupId);
        if (chatGroup == null) {
            return ResultUtil.Fail("群聊不存在");
        }
        if(!chatGroup.getOwnerUserId().equals(userId)) {
            throw new BaseException("您不是群主~");
        }

        if(StringUtils.isNotBlank(chatGroup.getPortrait())){
            minioUtil.remove(chatGroup.getPortrait());
        }
        //添加缓存破坏参数 防止浏览器缓存旧头像。每次更新头像后，URL 都会不同，强制刷新。
        String fileName = groupId + "-portrait" +  System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        minioUtil.upload(file.getInputStream(), fileName, type, size);
        chatGroupService.updateGroupPortrait(groupId, fileName);
        return ResultUtil.Succeed(fileName);
    }
}
