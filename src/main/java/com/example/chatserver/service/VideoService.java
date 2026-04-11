package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.vo.video.*;
import com.example.chatserver.websocket.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
/*
呼叫方 (A)                   信令服务器                    接听方 (B)
        │                              │                              │
        │  1. InviteVo (邀请)           │                              │
        │ ─────────────────────────────→│  1. InviteVo (转发邀请)       │
        │                              │ ─────────────────────────────→│
        │                              │                              │
        │                              │  2. AcceptVo (接受)           │
        │ ←─────────────────────────────│ ←─────────────────────────────│
        │                              │                              │
        │  3. OfferVo (SDP提议)         │                              │
        │ ─────────────────────────────→│  3. OfferVo (转发SDP)         │
        │                              │ ─────────────────────────────→│
        │                              │                              │
        │                              │  4. AnswerVo (SDP应答)        │
        │ ←─────────────────────────────│ ←─────────────────────────────│
        │                              │                              │
        │  5. CandidateVo (ICE候选)     │                              │
        │ ←─────────────────────────────│ ←─────────────────────────────│
        │                              │                              │
        │  6. 建立 P2P 连接              │                              │
        │ ←───────────────────────────→│                              │*/
@Service
public class VideoService {

    @Resource
    FriendService friendService;

    @Resource
    WebSocketService webSocketService;

    //这里的msg不会录入数据库，只是用来双方传消息
    //最终加入数据库的msg是前端发送过来，后端保存
    //这里的类型是消息的类型，总的类型video由WebSocket设置

    public boolean invite(String userId, InviteVo inviteVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, inviteVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "invite");
        msg.set("fromId", userId);
        msg.set("isOnlyAudio", inviteVo.isOnlyAudio());
        if(!webSocketService.isOnline(inviteVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, inviteVo.getUserId());
        return true;
    }

    public boolean accept(String userId, AcceptVo acceptVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, acceptVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "accept");
        msg.set("fromId", userId);
        if(!webSocketService.isOnline(acceptVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, acceptVo.getUserId());
        return true;
    }

    public boolean offer(String userId, OfferVo offerVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, offerVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "offer");
        //desc 包含了通话的媒体配置信息，比如用什么编码、网络地址、端口等。
        msg.set("desc", offerVo.getDesc());
        msg.set("fromId", userId);
        if(!webSocketService.isOnline(offerVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, offerVo.getUserId());
        return true;
    }

    public boolean answer(String userId, AnswerVo answerVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, answerVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "answer");
        //desc 包含了通话的媒体配置信息，比如用什么编码、网络地址、端口等。
        msg.set("desc", answerVo.getDesc());
        msg.set("fromId", userId);
        if(!webSocketService.isOnline(answerVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, answerVo.getUserId());
        return true;
    }

    public boolean candidate(String userId, CandidateVo candidateVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, candidateVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "candidate");
        //candidate 是 ICE Candidate（ICE 候选者），包含了网络地址信息，用于帮助双方找到彼此并建立 P2P 连接。
        msg.set("candidate", candidateVo.getCandidate());
        msg.set("fromId", userId);
        if(!webSocketService.isOnline(candidateVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, candidateVo.getUserId());
        return true;
    }

    public boolean hangup(String userId, HangupVo hangupVo) {
        boolean isFriend = friendService.isFriendIgnoreSpecial(userId, hangupVo.getUserId());
        if (!isFriend) {
            throw new BaseException("双方非好友");
        }
        JSONObject msg = new JSONObject();
        msg.set("type", "hangup");
        msg.set("fromId", userId);
        if(!webSocketService.isOnline(hangupVo.getUserId())){
            throw new BaseException("对方不在线或连接异常");
        }
        webSocketService.sendVideoToUser(msg, hangupVo.getUserId());
        return true;
    }

}
