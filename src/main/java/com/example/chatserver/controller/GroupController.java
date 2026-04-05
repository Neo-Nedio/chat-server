package com.example.chatserver.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.service.GroupService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.group.CreateGroupVo;
import com.example.chatserver.vo.group.DeleteGroupVo;
import com.example.chatserver.vo.group.UpdateGroupVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/api/group")
public class GroupController {
    @Resource
    GroupService groupService;

    @PostMapping("/create")
    public JSONObject createGroup(@Userid String userId, @RequestBody CreateGroupVo createGroupVo) {
        boolean flag = groupService.createGroup(userId, createGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/update")
    public JSONObject updateGroup(@Userid String userId, @RequestBody UpdateGroupVo updateGroupVo) {
        boolean flag = groupService.updateGroup(userId, updateGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }

    @PostMapping("/delete")
    public JSONObject deleteGroup(@Userid String userId, @RequestBody DeleteGroupVo deleteGroupVo) {
        boolean flag = groupService.deleteGroup(userId, deleteGroupVo);
        return ResultUtil.ResultByFlag(flag);
    }
}

