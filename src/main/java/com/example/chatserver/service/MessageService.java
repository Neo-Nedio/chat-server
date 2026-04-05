package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.Message;
import com.example.chatserver.vo.message.SendMsgToUserVo;

public interface MessageService extends IService<Message> {

    boolean sendMessageToUser(String userId, SendMsgToUserVo sendMsgToUserVo);
}
