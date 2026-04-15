package com.example.chatserver.admin.controller;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.chatserver.admin.vo.user.*;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.constant.UserRole;
import com.example.chatserver.entity.User;
import com.example.chatserver.service.UserService;
import com.example.chatserver.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("AdminUserController")
@RequestMapping("/admin/v1/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    @GetMapping("isAdmin")
    public JSONObject isAdmin(@Userid String UserId) {
        User user = userService.getById(UserId);
        boolean result = user.getRole().equals(UserRole.Admin);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/page")
    @UrlResource("admin")
    public JSONObject userList(@RequestBody UserListVo userListVo) {
        Page<User> result = userService.userList(userListVo);
        return ResultUtil.Succeed(result);
    }


    @PostMapping("/create")
    @UrlResource("admin")
    public JSONObject createUser(@RequestBody CreateUserVo createUserVo) {
        boolean result = userService.createUser(createUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/update")
    @UrlResource("admin")
    public JSONObject updateUser(@RequestBody UpdateUserVo updateUserVo) {
        boolean result = userService.updateUser(updateUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/disable")
    @UrlResource("admin")
    public JSONObject disableUser(@Userid String userid, @RequestBody DisableUserVo disableUserVo) {
        boolean result = userService.disableUser(userid, disableUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/unDisable")
    @UrlResource("admin")
    public JSONObject unDisableUser(@RequestBody UnDisableUserVo unDisableUser) {
        boolean result = userService.unDisableUser(unDisableUser);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/delete")
    @UrlResource("admin")
    public JSONObject deleteUser(@Userid String userid, @RequestBody DeleteUserVo deleteUserVo) {
        boolean result = userService.deleteUser(userid, deleteUserVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/reset/password")
    @UrlResource("admin")
    public JSONObject restPassword(@RequestBody ResetPasswordVo resetPasswordVo) {
        String result = userService.restPassword(resetPasswordVo);
        return ResultUtil.Succeed(result);
    }

    @PostMapping("/set/admin")
    @UrlResource("admin")
    public JSONObject setAdmin(@Userid String userid, @RequestBody SetAdminVo setAdminVo) {
        boolean result = userService.setAdmin(userid, setAdminVo);
        return ResultUtil.ResultByFlag(result);
    }

    @PostMapping("/cancel/admin")
    @UrlResource("admin")
    public JSONObject cancelAdmin(@Userid String userid, @RequestBody CancelAdminVo cancelAdminVo) {
        boolean result = userService.cancelAdmin(userid, cancelAdminVo);
        return ResultUtil.ResultByFlag(result);
    }
}
