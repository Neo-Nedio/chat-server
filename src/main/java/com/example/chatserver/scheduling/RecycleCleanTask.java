package com.example.chatserver.scheduling;

import com.example.chatserver.service.SpaceRecycleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RecycleCleanTask {

    @Resource
    SpaceRecycleService spaceRecycleService;

    /**
     * 每天凌晨3点30分清理已过期的回收站文件（保留30天到期后彻底删除）
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanExpiredRecycle() {
        spaceRecycleService.cleanExpiredRecycles();
    }
}
