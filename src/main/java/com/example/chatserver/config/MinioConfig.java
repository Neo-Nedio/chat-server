package com.example.chatserver.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio") //将配置文件中 minio.* 的属性绑定到这个类
public class MinioConfig {

    private String endpoint; //MinIO 服务地址
    private String accessKey; //访问密钥
    private String secretKey; //秘密密钥
    private String bucketName; //默认存储桶名称

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)  // 连接地址
                .credentials(accessKey, secretKey)  // 认证信息
                .build();
    }
}
