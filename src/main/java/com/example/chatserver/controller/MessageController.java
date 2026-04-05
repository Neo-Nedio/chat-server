package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.entity.Message;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.message.SendMsgToUserVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/v1/api/message")
@Slf4j
public class MessageController {

    @Resource
    MessageService messageService;

    /**
     * 发送消息给用户
     */
    @PostMapping("/send/to/user")
    public JSONObject sendMessageToUser(@Userid String userId, @RequestBody SendMsgToUserVo sendMsgToUserVo) {
        Message result = messageService.sendMessageToUser(userId, sendMsgToUserVo);
        return ResultUtil.Succeed(result);
    }

}
