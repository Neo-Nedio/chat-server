package com.example.chatserver.service.impl;

import com.example.chatserver.constant.CallAction;
import com.example.chatserver.constant.CallType;
import com.example.chatserver.dto.voip.CallInviteDto;
import com.example.chatserver.dto.voip.CallSignalDto;
import com.example.chatserver.dto.voip.LiveKitRoomUserDto;
import com.example.chatserver.dto.voip.LiveResultDto;
import com.example.chatserver.dto.voip.LiveRoomDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.service.LiveKitTokenService;
import com.example.chatserver.service.LiveRoomService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.service.VoipService;
import com.example.chatserver.utils.CallSessionUtil;
import com.example.chatserver.websocket.WebSocketService;
import com.example.chatserver.vo.voip.GroupCallHangupVo;
import com.example.chatserver.vo.voip.GroupCallInviteVo;
import com.example.chatserver.vo.voip.LiveKitTokenVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class VoipServiceImpl implements VoipService {
    @Resource
    ChatGroupMemberService chatGroupMemberService;
    @Resource
    ChatGroupService chatGroupService;
    @Resource
    UserService userService;
    @Resource
    WebSocketService webSocketService;
    @Resource
    LiveKitTokenService liveKitTokenService;
    @Resource
    LiveRoomService liveRoomService;

    @Override
    public CallInviteDto inviteGroup(String userId, GroupCallInviteVo vo) {
        // 校验发起人是不是群成员
        checkMember(vo.getGroupId(), userId);

        // 校验通话类型是否合法
        if (!CallType.isValid(vo.getCallType())) throw new BaseException("通话类型无效");

        // 查出该群全部成员，作为过滤白名单
        List<String> memberIds = memberIds(vo.getGroupId());

        // 过滤出有效的被邀请人：去 null、去空格、排除自己、必须是群成员、去重
        List<String> targets = (vo.getUserIds() == null ? List.<String>of() : vo.getUserIds()).stream()
                .filter(Objects::nonNull).map(String::trim)
                .filter(id -> !id.isBlank() && !id.equals(userId) && memberIds.contains(id)).distinct().toList();

        // 根据群 ID 生成群通话会话 ID
        String sessionId = CallSessionUtil.groupSession(vo.getGroupId());

        // 给被邀请人推送邀请信令
        sendToTargets(
                newSignal(
                        CallAction.Invite,
                        userId,
                        vo.getGroupId(),
                        sessionId,
                        targets,
                        vo.getCallType()
                ),
                targets
        );

        // 组装返回结果
        CallInviteDto result = new CallInviteDto();
        result.setSessionId(sessionId);
        result.setGroupId(vo.getGroupId());
        result.setCallType(vo.getCallType());
        result.setSceneType("group");
        return result;
    }

    @Override
    public void hangupGroup(String userId, GroupCallHangupVo vo) {
        // 只有群主可以结束整场群通话
        if (!chatGroupService.isOwner(vo.getGroupId(), userId)) {
            throw new BaseException("只有群主可以结束群通话");
        }

        // 群主结束整场通话，通知全体群成员（包括群主自己）退出 LiveKit
        List<String> memberIds = memberIds(vo.getGroupId());
        String sessionId = CallSessionUtil.groupSession(vo.getGroupId());

        sendToTargets(
                newSignal(CallAction.Hangup, userId, vo.getGroupId(), sessionId, memberIds, null),
                memberIds
        );
    }

    @Override public String getLiveKitHost() {
        return liveKitTokenService.getHost();
    }

    @Override
    public String getGroupToken(String userId, LiveKitTokenVo vo) {
        String groupId = CallSessionUtil.parseGroupId(vo.getSessionId());
        checkMember(groupId, userId);
        return liveKitTokenService.createToken(userId, vo.getSessionId());
    }

    @Override
    public List<LiveKitRoomUserDto> getRoomUsers(String userId, LiveKitTokenVo vo) {
        String groupId = CallSessionUtil.parseGroupId(vo.getSessionId()); checkMember(groupId, userId);
        return liveKitTokenService.listParticipants(vo.getSessionId());
    }

    @Override
    public LiveResultDto startLive(String userId) {
        String sessionId = CallSessionUtil.liveSession(userId);
        LiveResultDto result = new LiveResultDto();
        result.setSessionId(sessionId);
        result.setToken(liveKitTokenService.createToken(userId, sessionId, true));
        result.setSceneType("live");
        return result;
    }

    @Override
    public LiveResultDto getLiveToken(String userId, LiveKitTokenVo vo) {
        String sessionId = vo.getSessionId();
        CallSessionUtil.parseLiveUserId(sessionId);
        LiveResultDto result = new LiveResultDto();
        result.setSessionId(sessionId);
        result.setToken(liveKitTokenService.createToken(userId, sessionId, false));
        result.setSceneType("live");
        return result;
    }

    @Override
    public List<LiveRoomDto> getLiveRooms() {
        List<String> sessionIds = liveKitTokenService.listActiveLiveRooms();
        List<LiveRoomDto> roomInfos = liveRoomService.getLiveRooms(sessionIds);
        return roomInfos.stream().map(roomInfo -> {
            String sessionId = roomInfo.getSessionId();
            String userId = CallSessionUtil.parseLiveUserId(sessionId);
            List<LiveKitRoomUserDto> participants = liveKitTokenService.listParticipants(sessionId);
            boolean hostOnline = participants.stream()
                    .anyMatch(participant -> userId.equals(participant.getUserId())
                            && (participant.getState() == null || "ACTIVE".equalsIgnoreCase(participant.getState())));
            if (!hostOnline) return null;

            roomInfo.setParticipantCount(participants.size());
            return roomInfo;
        }).filter(java.util.Objects::nonNull).toList();
    }

    private void checkMember(String groupId, String userId) {
        if (!chatGroupMemberService.isMemberExists(groupId, userId))
            throw new BaseException("您不是群成员");
    }
    private List<String> memberIds(String groupId) {
        return chatGroupMemberService.getGroupMember(groupId)
                .stream()
                .map(ChatGroupMember::getUserId)
                .toList();
    }

    /**
     * 构造群通话信令
     * 把各个参数组装成一个统一的 CallSignalDto，场景固定为 "group"
     */
    private CallSignalDto newSignal(String action, String from, String group, String session, List<String> targets, String type) {
        CallSignalDto signal = new CallSignalDto();
        signal.setAction(action);           // 信令动作：邀请 / 变更 等
        signal.setFromUserId(from);         // 发起人
        signal.setGroupId(group);           // 群 ID
        signal.setSessionId(session);       // 会话 ID
        signal.setToUserIds(targets);       // 接收人列表
        signal.setCallType(type);           // 通话类型
        signal.setSceneType("group");       // 场景固定为群通话
        return signal;
    }

    /**
     * 把信令推送给指定的用户列表
     * 逐个通过 WebSocket 发下去
     */
    private void sendToTargets(CallSignalDto signal, List<String> targets) {
        targets.forEach(id -> webSocketService.sendCallToUser(signal, id));
    }
}
