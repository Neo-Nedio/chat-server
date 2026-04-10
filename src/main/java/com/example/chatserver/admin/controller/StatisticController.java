package com.example.chatserver.admin.controller;


import cn.hutool.json.JSONObject;
import com.example.chatserver.admin.vo.statistic.LoginDetailsVo;
import com.example.chatserver.annotation.UrlResource;
import com.example.chatserver.dto.UserOperatedDto;
import com.example.chatserver.service.UserOperatedService;
import com.example.chatserver.utils.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController("AdminStatisticController")
@RequestMapping("/admin/v1/api/stat")
@Slf4j
public class StatisticController {

    @Resource
    UserOperatedService userOperatedService;

    /**
     * 登录详情列表
     */
    @PostMapping("/login/details")
    @UrlResource("admin")
    public JSONObject loginDetails(@RequestBody LoginDetailsVo loginDetailsVo) {
        List<UserOperatedDto> result = userOperatedService.loginDetails(loginDetailsVo);
        return ResultUtil.Succeed(result);
    }


}
