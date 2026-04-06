package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.vo.message.MessageRecordVo;
import com.example.chatserver.vo.message.SendMsgToUserVo;

import java.util.List;

public interface MessageService extends IService<Message> {

    Message sendMessageToUser(String userId, SendMsgToUserVo sendMsgToUserVo);

    List<Message> messageRecord(String userId, MessageRecordVo messageRecordVo);

    Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo);

    MsgContent getFileMsgContent(String userId, String msgId);

    boolean updateMsgContent(String msgId, MsgContent msgContent);
}
