package com.example.chatserver.service;


import com.example.chatserver.admin.vo.statistic.LoginDetailsVo;
import com.example.chatserver.dto.UserOperatedDto;

import java.util.List;

public interface UserOperatedService {
    boolean recordLogin(String id, String ip);

    List<UserOperatedDto> loginDetails(LoginDetailsVo loginDetailsVo);
}
