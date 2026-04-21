package com.example.chatserver.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.constant.MessageContentType;
import com.example.chatserver.constant.MsgSource;
import com.example.chatserver.constant.MsgType;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.dto.SystemMsgDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.entity.User;
import com.example.chatserver.entity.ext.MsgContent;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.ChatGroupMemberMapper;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.service.ChatGroupService;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.RedisUtils;
import com.example.chatserver.vo.ChatListMember.BanMemberVo;
import com.example.chatserver.vo.ChatListMember.MemberListVo;
import com.example.chatserver.vo.message.SendMsgVo;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember> implements ChatGroupMemberService {

    @Resource
    ChatGroupMemberMapper chatGroupMemberMapper;

    @Lazy
    @Resource
    ChatGroupService chatGroupService;

    @Lazy
    @Resource
    MessageService messageService;

    @Lazy
    @Resource
    UserService userService;

    @Resource
    RedisUtils redisUtils;

    @Override
    public List<ChatGroupMember> getGroupMember(String groupId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getChatGroupId, groupId);
        return list(queryWrapper);
    }

    @Override
    public Map<String, MemberListDto> memberList(String userId, MemberListVo memberListVo) {
        List<MemberListDto> result = chatGroupMemberMapper.memberList(userId, memberListVo.getChatGroupId());
        //将群成员列表转换为 Map 结构的方法，方便前端按用户 ID 快速查找成员信息
        return result.stream().collect(Collectors.toMap(MemberListDto::getUserId, user -> user));
    }

    @Override
    public List<MemberListDto> memberListPage(String userId, MemberListVo memberListVo) {
        return chatGroupMemberMapper.memberListPage(userId, memberListVo);
    }

    @Override
    public boolean isMemberExists(String groupId, String userId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, groupId);
        return count(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean ban(String userId, BanMemberVo banMemberVo) {
        if (!chatGroupService.isOwner(banMemberVo.getGroupId(), userId))
            throw new BaseException("您不是群主~");
        if(userId.equals(banMemberVo.getTargetId())){
            throw new BaseException("不能禁言自己~");
        }

        //设置禁言
        LambdaUpdateWrapper<ChatGroupMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(ChatGroupMember::getBanEndTime, banMemberVo.getBanEndTime())
                .eq(ChatGroupMember::getChatGroupId, banMemberVo.getGroupId())
                .eq(ChatGroupMember::getUserId, banMemberVo.getTargetId());

        boolean result = update(wrapper);

        // 删除缓存
        if (result) {
            String banKey = "group:ban:" + banMemberVo.getGroupId() + ":" + banMemberVo.getTargetId();
            redisUtils.del(banKey);
        }

        //发送群消息
        SendMsgVo sendMsgVo = new SendMsgVo();
        sendMsgVo.setSource(MsgSource.Group);
        sendMsgVo.setToUserId(banMemberVo.getGroupId());
        MsgContent msgContent = new MsgContent();
        msgContent.setType(MessageContentType.Quit);
        User user = userService.getById(banMemberVo.getTargetId());
        //设置系统消息
        SystemMsgDto systemMsgDto = new SystemMsgDto();
        systemMsgDto.addEmphasizeContent(user.getName())
                .addContent(banMemberVo.getBanMessage());
        msgContent.setContent(JSONUtil.toJsonStr(systemMsgDto.getContents()));
        msgContent.setFromUserId(userId);
        msgContent.setExt(banMemberVo.getTargetId());
        sendMsgVo.setMsgContent(msgContent);
        messageService.sendMessage(userId, UserRole.User, sendMsgVo, MsgType.System);

        return result;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean isBan(BanMemberVo banMemberVo) {
        LambdaQueryWrapper<ChatGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatGroupMember::getChatGroupId, banMemberVo.getGroupId())
                .eq(ChatGroupMember::getUserId, banMemberVo.getTargetId());

        ChatGroupMember member = getOne(wrapper);
        if (member == null) {
            throw new BaseException("该用户不在群聊中");
        }

        Date banEndTime = member.getBanEndTime();
        // banEndTime不为null 且 禁言截止时间 > 当前时间，表示正在禁言中
        return banEndTime != null && banEndTime.after(new Date());
    }
}
