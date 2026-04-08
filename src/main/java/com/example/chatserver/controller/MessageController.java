package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.entity.Message;
import com.example.chatserver.entity.MessageRetraction;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.utils.MinioUtil;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.message.MessageRecordVo;
import com.example.chatserver.vo.message.ReeditMsgVo;
import com.example.chatserver.vo.message.RetractionMsgVo;
import com.example.chatserver.vo.message.SendMsgToUserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@RestController
@RequestMapping("/v1/api/message")
@Slf4j
public class MessageController {

    @Resource
    MessageService messageService;

    @Resource
    MinioUtil minioUtil;

    @Resource
    RedisUtils redisUtils;

    /**
     * 发送消息给用户
     */
    @PostMapping("/send/to/user")
    public JSONObject sendMessageToUser(@Userid String userId, @RequestBody SendMsgToUserVo sendMsgToUserVo) {
        Message result = messageService.sendMessageToUser(userId, sendMsgToUserVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 撤回消息
     */
    @PostMapping("/retraction")
    public JSONObject retractionMsg(@Userid String userId, @RequestBody RetractionMsgVo retractionMsgVo) {
        Message result = messageService.retractionMsg(userId, retractionMsgVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 重新编辑
     */
    @PostMapping("/reedit")
    public JSONObject reeditMsg(@Userid String userId, @RequestBody ReeditMsgVo reeditMsgVo) {
        MessageRetraction result = messageService.reeditMsg(userId, reeditMsgVo);
        return ResultUtil.Succeed(result);
    }


    /**
     * 聊天记录
     */
    @PostMapping("/record")
    public JSONObject messageRecord(@Userid String userId, @RequestBody MessageRecordVo messageRecordVo) {
        List<Message> result = messageService.messageRecord(userId, messageRecordVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 聊天记录（降序）
     */
    @PostMapping("/record/desc")
    public JSONObject messageRecordDesc(@Userid String userId, @RequestBody MessageRecordVo messageRecordVo) {
        List<Message> result = messageService.messageRecordDesc(userId, messageRecordVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 发送文件
     */
    @PostMapping("/send/file")
    public JSONObject sendFile(HttpServletRequest request,
                               @Userid String userId,
                               @RequestHeader("msgId") String msgId) throws IOException {
        String url = messageService.sendFileOrImg(userId, msgId, request);
        return ResultUtil.Succeed(url);
    }

    /**
     * 发送图片
     */
    @PostMapping(value = "/send/Img")
    public JSONObject sendImg(HttpServletRequest request,
                              @Userid String userId,
                              @RequestHeader("msgId") String msgId) throws IOException {
        String url = messageService.sendFileOrImg(userId, msgId, request);
        return ResultUtil.Succeed(url);
    }

    /**
     * 获取文件
     */
    @GetMapping("/get/file")
    public ResponseEntity<InputStreamResource> getFile(HttpServletResponse response,
                                                       @Userid String userId,
                                                       @RequestHeader("msgId") String msgId) {
        MsgContent msgContent = messageService.getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        String fileName = fileInfo.get("fileName").toString();
        InputStream inputStream = minioUtil.getObject(fileName);
        //返回文件作为下载
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.get("name").toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    /**
     * 获取媒体
     */
    @GetMapping("/get/media")
    public JSONObject getMedia(@Userid String userId, @RequestParam("msgId") String msgId) {
        MsgContent msgContent = messageService.getFileMsgContent(userId, msgId);
        JSONObject fileInfo = JSONUtil.parseObj(msgContent.getContent());
        String fileName = fileInfo.get("fileName").toString();
        String url = (String) redisUtils.get(fileName);
        if (StringUtils.isBlank(url)) {
            url = minioUtil.previewFile(fileName);
            redisUtils.set(fileName, url, 7 * 24 * 60);
        }
        return ResultUtil.Succeed(url);
    }

    /**
     * 语音消息转文字
     */
    @GetMapping("/voice/to/text")
    public JSONObject voiceToText(@Userid String userId, @RequestParam("msgId") String msgId) {
        Message result = messageService.voiceToText(userId, msgId);
        return ResultUtil.Succeed(result);
    }

}
