package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.entity.SpaceRecycle;

import java.util.List;


/**
 * 个人云盘回收站：删除的文件保留30天，期间可还原；过期记录不可见且每日自动彻底删除。
 */
public interface SpaceRecycleService extends IService<SpaceRecycle> {

    /**
     * 查询回收站列表（过期记录不展示）
     */
    List<SpaceRecycle> listUserRecycle(String userId);

    /**
     * 还原回收站文件（还原前校验空间容量是否足够）
     */
    void restoreUserRecycle(String userId, List<String> spaceRecycleIds);

    /**
     * 彻底删除回收站文件（物理文件引用计数减1，记录物理删除）
     */
    void permanentlyDeleteUserRecycle(String userId, List<String> spaceRecycleIds);

    /**
     * 清空回收站
     */
    void clearUserRecycle(String userId);

    /**
     * 清理已过期的回收站记录（定时任务调用）
     */
    void cleanExpiredRecycles();
}
