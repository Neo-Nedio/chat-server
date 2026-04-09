package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.vo.ChatListMember.MemberListVo;

import java.util.List;
import java.util.Map;

public interface ChatGroupMemberService extends IService<ChatGroupMember> {

    List<ChatGroupMember> getGroupMember(String groupId);

    Map<String, MemberListDto> memberList(String userId, MemberListVo memberListVo);

    List<MemberListDto> memberListPage(String userId, MemberListVo memberListVo);
}
