package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.MemberListDto;
import com.example.chatserver.entity.ChatGroupMember;
import com.example.chatserver.vo.ChatListMember.MemberListVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface ChatGroupMemberMapper extends BaseMapper<ChatGroupMember> {

    @Select("SELECT cgm.*,u.`name`,f.`remark`,f.`id` as friend_id,u.`portrait` FROM `chat_group_member` AS cgm " +
            "LEFT JOIN `friend` as f on f.`friend_id` = cgm.`user_id` and f.`user_id` = #{userId} " +
            "LEFT JOIN `user` as u on u.`id` = cgm.`user_id`" +
            "WHERE cgm.`chat_group_id` = #{chatGroupId} " +
            "ORDER BY cgm.`create_time` ASC ")
    List<MemberListDto> memberList(String userId, String chatGroupId);

    @Select("SELECT cgm.*,u.`name`,u.`portrait` FROM `chat_group_member` AS cgm " +
            "LEFT JOIN `user` as u on u.`id` = cgm.`user_id`" +
            "WHERE cgm.`chat_group_id` = #{memberListVo.chatGroupId} " +
            "ORDER BY cgm.`create_time` ASC " +
            "LIMIT #{memberListVo.index}, #{memberListVo.num} ")
    List<MemberListDto> memberListPage(String userId, MemberListVo memberListVo);
}
