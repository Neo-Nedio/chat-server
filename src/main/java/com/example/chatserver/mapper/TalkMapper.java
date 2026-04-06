package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.TalkListDto;
import com.example.chatserver.entity.Talk;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TalkMapper extends BaseMapper<Talk> {

    @Select("SELECT  " +
            "    u.name, " +
            "    u.id, " +
            "    t.user_id, " +
            "    u.portrait, " +

            //-- 备注：如果是自己发的，备注为 NULL；如果是好友发的，显示备注名
            "    CASE  " +
            "        WHEN t.user_id =  #{userId} THEN NULL  " +
            "        ELSE f.remark  " +
            "    END AS remark, " +

            "    t.id AS talk_id, " +
            "    t.`latest_comment`, " +
            "    t.`create_time` AS `time`, " +
            "    t.content, " +
            "    t.comment_num " +
            "FROM  " +
            "    talk AS t " +
            "LEFT JOIN  " +
            "    friend AS f ON (t.user_id = f.friend_id AND f.user_id =  #{userId}) " +
            "LEFT JOIN  " +
            "    user AS u ON u.id = t.user_id " +
            "LEFT JOIN  " +
            "    talk_permission AS tp ON t.id = tp.talk_id " +
            "WHERE  " +
            "    t.user_id =  #{userId}  " +
            "    OR (f.user_id =  #{userId} AND (tp.permission =  #{userId} OR tp.permission = 'all')) " +
            "ORDER BY  " +
            "    t.create_time DESC " +
            "LIMIT #{index}, #{num} ")
    @ResultMap("TalkListDtoResultMap")
    List<TalkListDto> talkList(String userId, int index, int num);
}
