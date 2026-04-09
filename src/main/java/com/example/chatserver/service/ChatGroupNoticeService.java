package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.ChatGroupNotice;
import com.example.chatserver.vo.chatGroupNotice.CreateNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.DeleteNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.NoticeListVo;
import com.example.chatserver.vo.chatGroupNotice.UpdateNoticeVo;

import java.util.List;

public interface ChatGroupNoticeService extends IService<ChatGroupNotice> {

    List<ChatGroupNotice> noticeList(String userId, NoticeListVo noticeListVo);

    boolean createNotice(String userId, CreateNoticeVo createNoticeVo);

    boolean deleteNotice(String userId, DeleteNoticeVo deleteNoticeVo);

    boolean updateNotice(String userId, UpdateNoticeVo updateNoticeVo);
}
