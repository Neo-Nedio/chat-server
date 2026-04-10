package com.example.chatserver.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.admin.vo.expose.ThirdSendMsgVo;
import com.example.chatserver.dto.Top10MsgDto;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.MessageRetraction;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.vo.message.MessageRecordVo;
import com.example.chatserver.vo.message.ReeditMsgVo;
import com.example.chatserver.vo.message.RetractionMsgVo;
import com.example.chatserver.vo.message.SendMsgVo;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface MessageService extends IService<Message> {

    Message sendMessage(String userId,  String role ,SendMsgVo sendMsgVo, String type);

    List<Message> messageRecord(String userId, MessageRecordVo messageRecordVo);

    List<Message> messageRecordDesc(String userId, MessageRecordVo messageRecordVo);

    Message sendFileMessageToUser(String userId, String toUserId, JSONObject fileInfo);

    MsgContent getFileMsgContent(String userId, String msgId);

    boolean updateMsgContent(String msgId, MsgContent msgContent);

    Message retractionMsg(String userId, RetractionMsgVo retractionMsgVo);

    MessageRetraction reeditMsg(String userId, ReeditMsgVo reeditMsgVo);

    String sendFileOrImg(String userId, String msgId, HttpServletRequest request) throws IOException;

    Message voiceToText(String userId, String msgId);

    Integer messageNum(DateTime date);

    List<Top10MsgDto> getTop10Msg(Date date);

    boolean thirdPartySendMsg(String userId, ThirdSendMsgVo sendMsgVo);
}
