package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.GroupListDto;
import com.example.chatserver.service.GroupService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.group.CreateGroupVo;
import com.example.chatserver.vo.group.DeleteGroupVo;
import com.example.chatserver.vo.group.UpdateGroupVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/api/group")
public class GroupController {
    @Resource
    GroupService groupService;

    @PostMapping("/create")
    public JSONObject createGroup(@Userid String userId, @Valid @RequestBody CreateGroupVo createGroupVo) {
        boolean flag = groupService.createGroup(userId, createGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/update")
    public JSONObject updateGroup(@Userid String userId, @Valid @RequestBody UpdateGroupVo updateGroupVo) {
        boolean flag = groupService.updateGroup(userId, updateGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/delete")
    public JSONObject deleteGroup(@Userid String userId, @Valid @RequestBody DeleteGroupVo deleteGroupVo) {
        boolean flag = groupService.deleteGroup(userId, deleteGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    /**
     * 获取用户分组
     */
    @GetMapping("/list")
    public JSONObject getList(@Userid String userId) {
        List<GroupListDto> result = groupService.getList(userId);
        return ResultUtil.Succeed(result);
    }
}

