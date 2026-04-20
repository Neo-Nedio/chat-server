package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.admin.vo.notify.DeleteNotifyVo;
import com.example.chatserver.constant.FriendApplyStatus;
import com.example.chatserver.constant.NotifyType;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.dto.ApplyNotifyDto;
import com.example.chatserver.dto.SystemNotifyDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.entity.Notify;
import com.example.chatserver.entity.User;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.NotifyMapper;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.service.FriendService;
import com.example.chatserver.service.NotifyService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.vo.chatGroup.DissolveChatGroupVo;
import com.example.chatserver.vo.notify.FriendApplyNotifyVo;
import com.example.chatserver.vo.notify.GroupApplyNotifyVo;
import com.example.chatserver.vo.notify.ReadNotifyVo;
import com.example.chatserver.websocket.WebSocketService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notify> implements NotifyService {

    @Lazy
    @Resource
    FriendService friendService;

    @Lazy
    @Resource
    UserService userService;

    @Lazy
    @Resource
    ChatGroupMemberService chatGroupMemberService;

    @Lazy
    @Resource
    ChatGroupService chatGroupService;

    @Resource
    NotifyMapper notifyMapper;

    @Resource
    WebSocketService webSocketService;

    @Resource
    RedisUtils redisUtils;

    @Override
    //发送好友申请
    public boolean friendApplyNotify(String userId,String userRole, FriendApplyNotifyVo friendApplyNotifyVo) {
        //验证是否是好友
        String friendKey = "friend:" + userId + ":" + friendApplyNotifyVo.getUserId();
        Boolean isFriend = (Boolean) redisUtils.get(friendKey);
        if (isFriend == null) {
            isFriend = friendService.isFriendIgnoreSpecial(userId, friendApplyNotifyVo.getUserId());
            redisUtils.set(friendKey, isFriend, 30 * 60);
        }
        if (isFriend) {
            throw new BaseException("ta已是您的好友");
        }

        //管理员直接添加好友，不发送请求
        if(UserRole.Admin.equals(userRole)){
            friendService.addFriendApply(userId,friendApplyNotifyVo.getUserId());
            return true;
        }

        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setFromId(userId);
        notify.setToId(friendApplyNotifyVo.getUserId());
        notify.setType(NotifyType.Friend_Apply);
        notify.setStatus(FriendApplyStatus.Wait);
        notify.setContent(friendApplyNotifyVo.getContent());
        notify.setUnreadId(friendApplyNotifyVo.getUserId());
        webSocketService.sendNotifyToUser(notify, friendApplyNotifyVo.getUserId());
        return save(notify);
    }

    @Override
    public boolean groupApplyNotify(String userId, String userRole, GroupApplyNotifyVo groupApplyNotifyVo) {
        String memberKey = "member:" + groupApplyNotifyVo.getGroupId() + ":" + userId;
        Boolean isMember = (Boolean) redisUtils.get(memberKey);
        if (isMember == null) {
            isMember = chatGroupMemberService.isMemberExists(groupApplyNotifyVo.getGroupId(), userId);
            redisUtils.set(memberKey, isMember, 30 * 60);
        }
        if (isMember) {
            throw new BaseException("你已经在群聊内");
        }
        String dissolvedKey = "group-dissolved:" +  groupApplyNotifyVo.getGroupId();
        Boolean dissolved = (Boolean) redisUtils.get(dissolvedKey);
        if (dissolved == null) {
            DissolveChatGroupVo dissolveChatGroupVo = new DissolveChatGroupVo();
            dissolveChatGroupVo.setGroupId(groupApplyNotifyVo.getGroupId());
            dissolved = chatGroupService.isDissolveChatGroup(dissolveChatGroupVo);
            redisUtils.set(dissolvedKey, dissolved, 30 * 60);
        }
        if (dissolved) {
            throw new BaseException("该群已解散");
        }

        //管理员直接加入群聊，不发送申请、不产生通知
        if(UserRole.Admin.equals(userRole)){
            return chatGroupService.joinGroup(groupApplyNotifyVo.getGroupId(), userId);
        }

        //获取群主id，把入群申请通知发给群主
        String ownerUserId = chatGroupService.getOwnerUserId(groupApplyNotifyVo.getGroupId());

        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setFromId(userId);
        notify.setToId(groupApplyNotifyVo.getGroupId());
        notify.setType(NotifyType.Group_Apply);
        notify.setStatus(FriendApplyStatus.Wait);
        notify.setContent(groupApplyNotifyVo.getContent());
        notify.setUnreadId(ownerUserId);

        webSocketService.sendNotifyToUser(notify, ownerUserId);
        return save(notify);
    }

    @Override
    //申请列表（好友申请 + 入群申请，按时间倒序）
    public List<ApplyNotifyDto> applyListNotify(String userId) {
        List<ApplyNotifyDto> friendList = notifyMapper.friendListNotify(userId, NotifyType.Friend_Apply);
        List<ApplyNotifyDto> groupList = notifyMapper.groupListNotify(userId, NotifyType.Group_Apply);

        List<ApplyNotifyDto> result = new ArrayList<>(friendList.size() + groupList.size());
        result.addAll(friendList);
        result.addAll(groupList);

        //按创建时间倒序，create_time 为 null 的排到最后
        result.sort(Comparator.comparing(Notify::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Override
    //未读通知数量
    public int unread(String userId) {
        Integer num = notifyMapper.unreadByUserId(userId);
        return num == null ? 0 : num;
    }

    @Override
    public int unreadByType(String userId, String type) {
        Integer num = notifyMapper.unreadByType(userId, type);
        return num == null ? 0 : num;
    }

    @Override
    //通知已读
    public boolean readNotify(String userId, ReadNotifyVo readNotifyVo) {
        LambdaUpdateWrapper<Notify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Notify::getUnreadId, "")
                .eq(Notify::getUnreadId, userId)
                .eq(Notify::getType, readNotifyVo.getNotifyType());
        return update(updateWrapper);
    }

    @Override
    public boolean groupNotifyRead(String userId, String groupId) {
        LambdaUpdateWrapper<ChatGroupMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(ChatGroupMember::getLastReadNoticeTime, new Date())
                .eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, groupId);

        return chatGroupMemberService.update(wrapper);
    }

    @Override
    public List<SystemNotifyDto> SystemListNotify(String userId) {
        return notifyMapper.SystemListNotify();
    }

    @Override
    public SystemNotifyDto SystemNotifyLatest(String userId) {
        List<SystemNotifyDto> list = notifyMapper.SystemListNotify();
        if (list == null || list.isEmpty()) {
            return null;
        }
        SystemNotifyDto latestNotify = list.get(0);
        User user = userService.getById(userId);
        // notifyReadTime 为 null 表示从未读过，直接返回通知
        if (user.getNotifyReadTime() != null
                && !user.getNotifyReadTime().before(latestNotify.getCreateTime())) {
            return null;
        }
        return latestNotify;
    }

    @Override
    public boolean SystemNotifyRead(String userId) {
        User user = new User();
        user.setId(userId);
        user.setNotifyReadTime(new Date());
        return userService.updateById(user);
    }

    @Override
    public boolean deleteNotify(DeleteNotifyVo deleteNotifyVo) {
        return removeById(deleteNotifyVo.getNotifyId());
    }

    @Override
    public boolean createNotify(String fileName, String title, String text) {
        SystemNotifyDto.SystemNotifyContent content = new SystemNotifyDto.SystemNotifyContent();
        content.setImg(fileName);
        content.setText(text);
        content.setTitle(title);
        Notify notify = new Notify();
        notify.setId(IdUtil.randomUUID());
        notify.setType(NotifyType.System);
        notify.setToId("all");
        notify.setFromId("system");
        notify.setContent(JSONUtil.toJsonStr(content));

        //发送系统通知给所有用户
        SystemNotifyDto dto = new SystemNotifyDto();
        dto.setContent(content);  // SystemNotifyContent 对象
        dto.setCreateTime(new Date());
        webSocketService.sendSystemNotifyAll(dto);

        return save(notify);
    }
}
