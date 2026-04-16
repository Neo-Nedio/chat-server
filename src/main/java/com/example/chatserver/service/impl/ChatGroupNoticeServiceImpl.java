package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.entity.ChatGroup;
import com.example.chatserver.entity.ChatGroupNotice;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ChatGroupNoticeMapper;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.ChatGroupNoticeService;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.vo.chatGroupNotice.CreateNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.DeleteNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.NoticeListVo;
import com.example.chatserver.vo.chatGroupNotice.UpdateNoticeVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class ChatGroupNoticeServiceImpl extends ServiceImpl<ChatGroupNoticeMapper, ChatGroupNotice> implements ChatGroupNoticeService {

    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Resource
    ChatGroupNoticeMapper chatGroupNoticeMapper;

    @Resource
    ChatGroupService chatGroupService;

    @Override
    public List<ChatGroupNotice> noticeList(String userId, NoticeListVo noticeListVo) {
        boolean isMemberExists = chatGroupMemberService.isMemberExists(noticeListVo.getGroupId(), userId);
        if (!isMemberExists)
            throw new BaseException("非该群成员~");
        return chatGroupNoticeMapper.noticeList(noticeListVo.getGroupId());
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean createNotice(String userId, CreateNoticeVo createNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(createNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");

        ChatGroupNotice chatGroupNotice = new ChatGroupNotice();
        chatGroupNotice.setId(IdUtil.randomUUID());
        chatGroupNotice.setChatGroupId(createNoticeVo.getGroupId());
        chatGroupNotice.setNoticeContent(createNoticeVo.getContent());
        chatGroupNotice.setUserId(userId);
        save(chatGroupNotice);
        //设置群公告，用来每次显示最新一条
        chatGroup.setNotice(chatGroupNotice);
        return chatGroupService.updateById(chatGroup);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean deleteNotice(String userId, DeleteNoticeVo deleteNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(deleteNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");

        //删除公告
        LambdaQueryWrapper<ChatGroupNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupNotice::getId, deleteNoticeVo.getNoticeId())
                .eq(ChatGroupNotice::getUserId, userId)
                .eq(ChatGroupNotice::getChatGroupId, deleteNoticeVo.getGroupId());
        remove(queryWrapper);

        //如果被删除公告是最新的，找最新一条替代
        if (deleteNoticeVo.getNoticeId().equals(chatGroup.getNotice().getId())) {
            List<ChatGroupNotice> result = chatGroupNoticeMapper.noticeList(deleteNoticeVo.getGroupId());
            if (result != null && !result.isEmpty()) {
                chatGroup.setNotice(result.get(0));
            } else {
                //清空noticeContent而不是Notice，直接传null有bug
                chatGroup.setNotice(chatGroup.getNotice().setNoticeContent(null));
            }
            return chatGroupService.updateById(chatGroup);
        }

        return true;
    }

    @Override
    public boolean updateNotice(String userId, UpdateNoticeVo updateNoticeVo) {
        ChatGroup chatGroup = chatGroupService.getById(updateNoticeVo.getGroupId());
        if (!chatGroup.getOwnerUserId().equals(userId))
            throw new BaseException("您不是群主~");

        //更新公告
        LambdaUpdateWrapper<ChatGroupNotice> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.set(ChatGroupNotice::getNoticeContent, updateNoticeVo.getNoticeContent())
                .eq(ChatGroupNotice::getUserId, userId)
                .eq(ChatGroupNotice::getChatGroupId, updateNoticeVo.getGroupId())
                .eq(ChatGroupNotice::getId, updateNoticeVo.getNoticeId());

        //如果更改的公告是最新的，也进行更新
        if (updateNoticeVo.getNoticeId().equals(chatGroup.getNotice().getId())) {
            chatGroup.getNotice().setNoticeContent(updateNoticeVo.getNoticeContent());
            chatGroupService.updateById(chatGroup);
        }
        return update(queryWrapper);
    }
}
