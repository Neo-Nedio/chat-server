package com.example.chatserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.chatserver.dto.ChatGroupDetailsDto;
import com.example.chatserver.entity.ChatGroup;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface ChatGroupMapper extends BaseMapper<ChatGroup> {

    @Select("SELECT * FROM chat_group " +
            "WHERE chat_group_number LIKE CONCAT('%', #{search}, '%') " +
            "   OR name LIKE CONCAT('%', #{search}, '%') " +
            "   AND status = 1"
            )
    List<ChatGroup> searchGroup(String search);

    @Select("SELECT cg.*, cgm.`group_remark` AS `group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cgm.`user_id` = #{userId} AND cg.`status` = 1")
    //获得自己的群聊列表
    List<ChatGroup> getList(String userId);

    @Select("<script>" +
            "SELECT cg.*, cgm.`group_remark` AS `group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cgm.`user_id` = #{userId} AND cg.`status` = 1" +
            "<if test=\"search != null and search != ''\">" +
            "AND (cg.`name` LIKE CONCAT('%', #{search}, '%') " +
            "OR cg.`chat_group_number` LIKE CONCAT('%', #{search}, '%') " +
            "OR cgm.`group_remark` LIKE CONCAT('%', #{search}, '%'))" +
            "</if>" +
            "</script>")
    //搜索自己的群聊列表
    List<ChatGroup> getListFromSearch(String userId, String search);

    @Select("SELECT cg.*, cgm.`group_name`, cgm.`group_remark` FROM `chat_group` AS cg " +
            "LEFT JOIN `chat_group_member` AS cgm ON cg.`id` = cgm.`chat_group_id` AND cgm.`user_id` = #{userId} " +
            "WHERE cg.`id` = #{chatGroupId} AND cg.`status` = 1")
    @ResultMap("ChatGroupDetailsDtoResultMap")
    ChatGroupDetailsDto detailsChatGroup(String userId, String chatGroupId);
}
