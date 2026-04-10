package com.example.chatserver.admin.controller;

import com.example.chatserver.admin.vo.expose.ThirdSendMsgVo;
import com.example.chatserver.annotation.UrlFree;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.utils.ResultUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/api/expose")
public class ExposeController {

    @Resource
    MessageService messengerService;

    @UrlFree
    @PostMapping("/send")
    public Object thirdPartySendMsg(@Userid String userid, @RequestBody ThirdSendMsgVo sendMsgVo) {
        boolean result = messengerService.thirdPartySendMsg(userid, sendMsgVo);
        return ResultUtil.ResultByFlag(result);
    }
}
