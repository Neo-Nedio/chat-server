package com.example.chatserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.mapper.ChatGroupMemberMapper;
import com.example.chatserver.service.ChatGroupMemberService;
import com.example.chatserver.vo.ChatListMember.MemberListVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember> implements ChatGroupMemberService {

    @Resource
    ChatGroupMemberMapper chatGroupMemberMapper;

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
}
