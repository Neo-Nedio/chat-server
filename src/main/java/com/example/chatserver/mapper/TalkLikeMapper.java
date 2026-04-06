package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.LikeListDto;
import com.example.chatserver.entity.TalkLike;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TalkLikeMapper extends BaseMapper<TalkLike> {

    @Select("SELECT tl.id, u.`name`, u.`portrait`, f.`remark`, u.`id` AS `user_id` " +
            "FROM `talk_like` AS tl " +
            "         LEFT JOIN `user` AS u ON u.`id` = tl.`user_id` " +
            "         LEFT JOIN `friend` AS f ON f.`user_id` = #{userId} AND f.`friend_id` = tl.`user_id` " +
            "WHERE tl.`talk_id` = #{talkId} ")
    List<LikeListDto> talkLikeList(String userId, String talkId);
}
