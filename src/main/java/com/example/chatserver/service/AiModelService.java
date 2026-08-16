package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.AiModelDto;
import com.example.chatserver.dto.LlmMessageDto;
import com.example.chatserver.entity.AiModel;
import com.example.chatserver.vo.ai.AddAiModelVo;
import com.example.chatserver.vo.ai.DeleteAiModelVo;
import com.example.chatserver.vo.ai.UpdateAiModelVo;

import java.io.IOException;
import java.util.List;


public interface AiModelService extends IService<AiModel> {

    /**
     * 流式输出回调
     */
    interface StreamCallback {
        void onDelta(String delta) throws IOException;
    }

    /**
     * 我的模型配置列表（不含 apiKey）
     */
    List<AiModelDto> myList(String userId);

    /**
     * 新增模型配置
     */
    boolean addModel(String userId, AddAiModelVo addAiModelVo);

    /**
     * 修改模型配置（校验归属，同步失效缓存）
     */
    boolean updateModel(String userId, UpdateAiModelVo updateAiModelVo);

    /**
     * 删除模型配置（校验归属，同步失效缓存）
     */
    boolean deleteModel(String userId, DeleteAiModelVo deleteAiModelVo);

    /**
     * 获取模型配置并校验归属，不存在或不属于当前用户则抛异常
     */
    AiModel getOwnedModel(String userId, String modelId);

    /**
     * 同步对话，返回完整回答
     */
    String chat(AiModel model, List<LlmMessageDto> messages);

    /**
     * 流式对话，增量内容通过回调下发
     */
    void chatStream(AiModel model, List<LlmMessageDto> messages, StreamCallback callback);
}