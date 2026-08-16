package com.example.chatserver.scheduling;

import com.example.chatserver.service.PhysicalFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class PhysicalFileCleanTask {

    @Resource
    PhysicalFileService physicalFileService;

    /**
     * 每天凌晨4点清理引用计数为0且已持续1天的物理文件（库表记录 + MinIO对象）
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanUnreferencedPhysicalFiles() {
        physicalFileService.cleanUnreferencedFiles();
    }
}
