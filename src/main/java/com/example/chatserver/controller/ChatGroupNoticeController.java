package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.entity.ChatGroupNotice;
import com.example.chatserver.service.ChatGroupNoticeService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.chatGroupNotice.CreateNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.DeleteNoticeVo;
import com.example.chatserver.vo.chatGroupNotice.NoticeListVo;
import com.example.chatserver.vo.chatGroupNotice.UpdateNoticeVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/v1/api/chat-group-notice")
public class ChatGroupNoticeController {

    @Resource
    ChatGroupNoticeService chatGroupNoticeService;


    /**
     * 创建群公告
     */
    @PostMapping("/create")
    public JSONObject createNotice(@Userid String userId, @Valid @RequestBody CreateNoticeVo createNoticeVo) {
        boolean result = chatGroupNoticeService.createNotice(userId, createNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 群公告列表
     */
    @PostMapping("/list")
    public JSONObject noticeList(@Userid String userId, @Valid @RequestBody NoticeListVo noticeListVo) {
        List<ChatGroupNotice> result = chatGroupNoticeService.noticeList(userId, noticeListVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 删除群公告
     */
    @PostMapping("/delete")
    public JSONObject deleteNotice(@Userid String userId, @Valid @RequestBody DeleteNoticeVo deleteNoticeVo) {
        boolean result = chatGroupNoticeService.deleteNotice(userId, deleteNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }


    /**
     * 编辑群公告
     */
    @PostMapping("/update")
    public JSONObject updateNotice(@Userid String userId, @Valid @RequestBody UpdateNoticeVo updateNoticeVo) {
        boolean result = chatGroupNoticeService.updateNotice(userId, updateNoticeVo);
        return ResultUtil.ResultByFlag(result);
    }
}
