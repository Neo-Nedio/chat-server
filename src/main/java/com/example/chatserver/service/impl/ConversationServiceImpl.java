package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.chatserver.admin.vo.conversation.DeleteConversationVo;
import com.example.chatserver.admin.vo.conversation.ResetSecretVo;
import com.example.chatserver.constant.ConversationStatus;
import com.example.chatserver.dto.ConversationDto;
import com.example.chatserver.entity.Conversation;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ConversationMapper;
import com.example.chatserver.service.ConversationService;
import com.example.chatserver.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Resource
    UserService userService;

    @Resource
    ConversationMapper conversationMapper;

    @Override
    public String getSecretKey(String accessKey) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getAccessKey, accessKey);
        Conversation conversation = getOne(queryWrapper);
        if (ConversationStatus.Normal.equals(conversation.getStatus())) return conversation.getSecretKey();
        return null;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Conversation createConversation(MultipartFile portrait, String name) {
        String userId = userService.createThirdPartyUser(portrait, name);
        Conversation conversation = new Conversation();
        conversation.setId(IdUtil.randomUUID());
        conversation.setUserId(userId);
        conversation.setAccessKey(IdUtil.randomUUID().replace("-", ""));
        conversation.setSecretKey(IdUtil.randomUUID().replace("-", ""));
        conversation.setStatus(ConversationStatus.Normal);
        save(conversation);
        return conversation;
    }

    @Override
    public List<ConversationDto> conversationList() {
        return conversationMapper.conversationList();
    }

    @Override
    public boolean updateConversation(MultipartFile portrait, String name, String id) {
        Conversation conversation = getById(id);
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        return userService.updateThirdPartyUser(portrait, name, conversation.getUserId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteConversation(DeleteConversationVo deleteConversationVo) {
        Conversation conversation = getById(deleteConversationVo.getConversationId());
        if (null == conversation) {
            throw new BaseException("会话不存在~");
        }
        boolean result = userService.deleteThirdPartyUser(conversation.getUserId());
        if (result) {
            return removeById(conversation.getId());
        }
        return false;
    }

    @Override
    public boolean resetSecret(ResetSecretVo resetSecretVo) {
        Conversation conversation = getById(resetSecretVo.getConversationId());
        conversation.setAccessKey(IdUtil.randomUUID().replace("-", ""));
        conversation.setSecretKey(IdUtil.randomUUID().replace("-", ""));
        return updateById(conversation);
    }
}
