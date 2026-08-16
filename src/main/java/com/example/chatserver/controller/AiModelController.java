package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.dto.AiModelDto;
import com.example.chatserver.service.AiModelService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.ai.AddAiModelVo;
import com.example.chatserver.vo.ai.DeleteAiModelVo;
import com.example.chatserver.vo.ai.UpdateAiModelVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/api/ai/model")
public class AiModelController {

    @Resource
    AiModelService aiModelService;

    /**
     * 我的模型配置列表
     */
    @GetMapping("/list")
    public JSONObject myList(@Userid String userId) {
        List<AiModelDto> list = aiModelService.myList(userId);
        return ResultUtil.Succeed(list);
    }

    /**
     * 新增模型配置
     */
    @PostMapping("/add")
    public JSONObject addModel(@Userid String userId, @Valid @RequestBody AddAiModelVo addAiModelVo) {
        boolean result = aiModelService.addModel(userId, addAiModelVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 修改模型配置
     */
    @PostMapping("/update")
    public JSONObject updateModel(@Userid String userId, @Valid @RequestBody UpdateAiModelVo updateAiModelVo) {
        boolean result = aiModelService.updateModel(userId, updateAiModelVo);
        return ResultUtil.ResultByFlag(result);
    }

    /**
     * 删除模型配置
     */
    @PostMapping("/delete")
    public JSONObject deleteModel(@Userid String userId, @Valid @RequestBody DeleteAiModelVo deleteAiModelVo) {
        boolean result = aiModelService.deleteModel(userId, deleteAiModelVo);
        return ResultUtil.ResultByFlag(result);
    }
}