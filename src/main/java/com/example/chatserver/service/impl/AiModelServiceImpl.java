package com.example.chatserver.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chatserver.dto.AiModelDto;
import com.example.chatserver.dto.LlmMessageDto;
import com.example.chatserver.entity.AiModel;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.mapper.AiModelMapper;
import com.example.chatserver.service.AiModelService;
import com.example.chatserver.vo.ai.AddAiModelVo;
import com.example.chatserver.vo.ai.DeleteAiModelVo;
import com.example.chatserver.vo.ai.UpdateAiModelVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {

    @Resource
    RestTemplate restTemplate;

    //模型配置缓存，update/delete 时必须失效
    private final ConcurrentHashMap<String, AiModel> modelCache = new ConcurrentHashMap<>();

    @Override
    public List<AiModelDto> myList(String userId) {
        LambdaQueryWrapper<AiModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiModel::getUserId, userId)
                .orderByDesc(AiModel::getCreateTime);
        //转成 dto 返回，不暴露 apiKey
        return list(queryWrapper).stream().map(model -> {
            AiModelDto dto = new AiModelDto();
            BeanUtils.copyProperties(model, dto);
            return dto;
        }).toList();
    }

    @Override
    public boolean addModel(String userId, AddAiModelVo addAiModelVo) {
        AiModel aiModel = new AiModel();
        BeanUtils.copyProperties(addAiModelVo, aiModel);
        aiModel.setId(IdUtil.randomUUID());
        aiModel.setUserId(userId);
        return save(aiModel);
    }

    @Override
    public boolean updateModel(String userId, UpdateAiModelVo updateAiModelVo) {
        getOwnedModel(userId, updateAiModelVo.getId());
        AiModel aiModel = new AiModel();
        BeanUtils.copyProperties(updateAiModelVo, aiModel);
        boolean result = updateById(aiModel);
        modelCache.remove(updateAiModelVo.getId());
        return result;
    }

    @Override
    public boolean deleteModel(String userId, DeleteAiModelVo deleteAiModelVo) {
        getOwnedModel(userId, deleteAiModelVo.getId());
        boolean result = removeById(deleteAiModelVo.getId());
        modelCache.remove(deleteAiModelVo.getId());
        return result;
    }

    @Override
    public AiModel getOwnedModel(String userId, String modelId) {
        AiModel model = modelCache.get(modelId);
        if (null == model) {
            model = getById(modelId);
            if (null == model) {
                throw new BaseException("模型配置不存在");
            }
            modelCache.put(modelId, model);
        }
        if (!userId.equals(model.getUserId())) {
            throw new BaseException("模型配置不存在");
        }
        return model;
    }

    @Override
    public String chat(AiModel model, List<LlmMessageDto> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(model.getApiKey());
        JSONObject response = restTemplate.postForObject(
                chatCompletionsUrl(model),
                new HttpEntity<>(buildBody(model, messages, false), headers),
                JSONObject.class
        );
        if (null == response) {
            throw new BaseException("模型响应为空");
        }
        String content = response.getByPath("choices[0].message.content", String.class);
        if (null == content) {
            throw new BaseException("模型响应解析失败:" + response);
        }
        return content;
    }

    @Override
    public void chatStream(AiModel model, List<LlmMessageDto> messages, StreamCallback callback) {
        restTemplate.execute(
                chatCompletionsUrl(model),
                HttpMethod.POST,
                request -> { // 设置请求头
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    request.getHeaders().setBearerAuth(model.getApiKey());
                    request.getHeaders().set("Accept", "text/event-stream");// 关键：表明需要流式响应
                    // 设置请求体（buildBody 第三个参数为 true）
                    request.getBody().write(JSONUtil.toJsonStr(buildBody(model, messages, true))
                            .getBytes(StandardCharsets.UTF_8));
                },
                response -> {
                    // 处理响应流
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while (null != (line = reader.readLine())) {  // 逐行读取
                            // 过滤非数据行
                            if (!line.startsWith("data:")) {
                                continue;
                            }
                            // 提取数据内容
                            String data = line.substring(5).trim();
                            // 结束标记
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            // 跳过空数据
                            if (data.isEmpty()) {
                                continue;
                            }
                            // 解析 JSON 块
                            JSONObject chunk = JSONUtil.parseObj(data);
                            // 提取增量内容
                            String delta = chunk.getByPath("choices[0].delta.content", String.class);
                            // 回调给调用方
                            if (null != delta && !delta.isEmpty()) {
                                callback.onDelta(delta);
                            }
                        }
                    }
                    return null;
                }
        );
    }

    /**
     * 组装 OpenAI 兼容请求体
     */
    private Map<String, Object> buildBody(AiModel model, List<LlmMessageDto> messages, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model.getModel());
        body.put("messages", messages);
        body.put("stream", stream);
        return body;
    }

    private String chatCompletionsUrl(AiModel model) {
        String baseUrl = model.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/chat/completions";
    }
}