package com.example.chatserver.runner;

import com.example.chatserver.utils.MinioUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
//应用启动后自动执行的初始化组件，用于将本地 minio 目录下的文件自动上传到 MinIO 服务器
public class MinioRunner implements ApplicationRunner {
    @Resource
    MinioUtil minioUtil;

    @Override
    public void run(ApplicationArguments args) {
        try {
            //获取资源目录
            Path resourcePath = Paths.get(new ClassPathResource("minio").getURI());
            Files.walk(resourcePath) // 递归遍历所有文件
                    .filter(Files::isRegularFile) // 只保留普通文件（排除目录）
                    .forEach(path -> { // 对每个文件执行操作
                        try {
                            // 保留子目录结构的objectName
                            //将绝对路径转换为相对路径，作为 MinIO 中的对象名
                            String objectName = resourcePath.relativize(path).toString().replace("\\", "/");
                            if (!minioUtil.isObjectExist(objectName)) { // 不存在才上传，避免重复
                                try (InputStream inputStream = Files.newInputStream(path)) {
                                    long size = Files.size(path);
                                    String contentType = Files.probeContentType(path);
                                    if (contentType == null) {
                                        contentType = "application/octet-stream";
                                    }
                                    minioUtil.upload(inputStream, objectName, contentType, size);
                                } catch (Exception e) {
                                    System.err.println("Error uploading file " + objectName + ": " + e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing file " + path + ": " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error occurred: " + e);
        }
        System.out.println("-----minio initialization is complete-----");
    }
}
