package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.entity.ChatGroupNotice;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatGroupNoticeMapper extends BaseMapper<ChatGroupNotice> {


    @Select("select cgn.*, u.`name`, u.`portrait` " +
            "from `chat_group_notice` as cgn " +
            "left join `user` as u on u.`id` = cgn.`user_id` " +
            "where cgn.`user_id` = #{userId} and cgn.`chat_group_id` = #{groupId} " +
            "order by cgn.`create_time` desc")
    List<ChatGroupNotice> noticeList(String userId, String groupId);
}
