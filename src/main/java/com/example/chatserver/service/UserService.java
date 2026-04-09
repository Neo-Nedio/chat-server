package com.example.chatserver.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.admin.vo.UserListVo;
import com.example.chatserver.dto.UserDto;
import com.example.chatserver.entity.User;
import com.example.chatserver.vo.login.LoginVo;
import com.example.chatserver.vo.user.RegisterVo;
import com.example.chatserver.vo.user.SearchUserVo;
import com.example.chatserver.vo.user.UpdateVo;

import java.util.HashMap;
import java.util.List;

public interface UserService extends IService<User> {
    boolean register(RegisterVo registerVo);

    JSONObject validateLogin(LoginVo loginVo, boolean isAdmin);

    List<UserDto> searchUser(SearchUserVo searchUserVo);

    HashMap<String, Integer> unreadInfo(String userId);

    UserDto info(String userId);

    boolean updateUserInfo(String userId, UpdateVo updateVo);

    boolean updateUserPortrait(String userId, String portrait);

    Page<User> userList(UserListVo userListVo);

    void offline(String userId);

    void online(String userId);
}
