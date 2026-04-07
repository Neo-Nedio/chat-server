package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.entity.UserSet;
import com.example.chatserver.service.UserSetService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.userSet.UpdateUserSetVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/v1/api/user-set")
@RestController
public class UserSetController {

    @Resource
    UserSetService userSetService;

    @GetMapping("")
    public JSONObject getUserSet(@Userid String userId) {
        UserSet result = userSetService.getUserSet(userId);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/update")
    public JSONObject updateUserSet(@Userid String userId, @RequestBody UpdateUserSetVo updateUserSetVo) {
        boolean result = userSetService.updateUserSet(userId, updateUserSetVo);
        return ResultUtil.ResultByFlag(result);
    }

}
