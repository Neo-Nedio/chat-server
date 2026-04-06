package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.user.SearchUserVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/v1/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    /**
     * 用户查询
     */
    @PostMapping("/search")
    public JSONObject searchUser(@RequestBody SearchUserVo searchUserVo) {
        List<UserDto> result = userService.searchUser(searchUserVo);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取用户每项未读数
     */
    @GetMapping("/unread")
    public JSONObject unreadInfo(@Userid String userId) {
        HashMap<String, Integer> result = userService.unreadInfo(userId);
        return ResultUtil.Succeed(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public JSONObject info(@Userid String userId) {
        UserDto result = userService.info(userId);
        return ResultUtil.Succeed(result);
    }
}
