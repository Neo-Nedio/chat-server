package com.example.chatserver.admin.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.admin.vo.conversation.DeleteConversationVo;
import com.example.chatserver.admin.vo.conversation.DisableConversationVo;
import com.example.chatserver.admin.vo.conversation.ResetSecretVo;
import com.example.chatserver.admin.vo.conversation.UnDisableConversationVo;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.dto.ConversationDto;
import com.example.chatserver.entity.Conversation;
import com.example.chatserver.service.ConversationService;
import com.example.chatserver.utils.ResultUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminConversationController")
@RequestMapping("/admin/v1/api/conversation")
@Slf4j
public class ConversationController {

    @Resource
    ConversationService conversationService;

    /**
     * 创建会话
     */
    @PostMapping("/create")
    @UrlResource("admin")
    public JSONObject createConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name) {
        Conversation result = conversationService.createConversation(portrait, name);
        return ResultUtil.Succeed(result);
    }

    /**
     * 修改会话
     */
    @PostMapping("/update")
    @UrlResource("admin")
    public JSONObject updateConversation(@NotNull(message = "头像不能为空~") @RequestParam("portrait") MultipartFile portrait,
                                         @NotNull(message = "名称不能为空~") @RequestParam("name") String name,
                                         @NotNull(message = "会话不能为空~") @RequestParam("id") String id) {
        boolean result = conversationService.updateConversation(portrait, name, id);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 会话列表
     */
    @GetMapping("/list")
    @UrlResource("admin")
    public JSONObject conversationList() {
        List<ConversationDto> result = conversationService.conversationList();
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除会话
     */
    @PostMapping("/delete")
    @UrlResource("admin")
    public JSONObject deleteConversation(@Valid @RequestBody DeleteConversationVo deleteConversationVo) {
        boolean result = conversationService.deleteConversation(deleteConversationVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 重置会话秘钥

     */
    @PostMapping("/reset/secret")
    @UrlResource("admin")
    public JSONObject resetSecret(@Valid @RequestBody ResetSecretVo resetSecretVo) {
        boolean result = conversationService.resetSecret(resetSecretVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 禁用会话
     */
    @PostMapping("/disable")
    @UrlResource("admin")
    public JSONObject disableConversation(@Valid @RequestBody DisableConversationVo disableConversationVo) {
        boolean result = conversationService.disableConversation(disableConversationVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 解禁会话
     */
    @PostMapping("/undisable")
    @UrlResource("admin")
    public JSONObject unDisableConversation(@Valid @RequestBody UnDisableConversationVo unDisableConversationVo) {
        boolean result = conversationService.unDisableConversation(unDisableConversationVo);
        return ResultUtil.ResultByFlag(result);
    }

}
