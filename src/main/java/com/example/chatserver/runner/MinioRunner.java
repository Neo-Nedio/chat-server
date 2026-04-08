package com.example.chatserver.runner;

import com.example.chatserver.utils.MinioUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Stream;

@Component
//应用启动后自动执行的初始化组件，用于将本地 minio 目录下的文件自动上传到 MinIO 服务器
public class MinioRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MinioRunner.class);

    @Resource
    MinioUtil minioUtil;

    @Override
    public void run(ApplicationArguments args) {
        try {
            //minio初始化 创建桶,设置桶策略
            minioUtil.init();
            //获取资源路径
            org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:minio/**");
            if (resources.length > 0) {
                URI uri = resources[0].getURI();
                if ("jar".equals(uri.getScheme())) {
                    processJarResources(uri); // JAR 包运行模式
                } else {
                    processFileSystemResources(resources); // IDE/本地运行模式
                }
            } else {
                logger.warn("No resources found in minio directory");
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("Error occurred while reading resources", e);
        }
        logger.info("-----minio initialization is complete-----");
    }

    private void processJarResources(URI uri) throws IOException, URISyntaxException {
        try (FileSystem fs = FileSystems.newFileSystem(
                //jar:file:/app.jar!/BOOT-INF/classes/minio/a.txt
                //split("!")[0] 取出 jar: 到 ! 之间的部分： jar:file:/app.jar
                URI.create(uri.toString().split("!")[0]), Collections.emptyMap())) {
            Path myPath = fs.getPath("/BOOT-INF/classes/minio"); //获取 JAR 包内部的 minio 目录路径
            if (Files.exists(myPath)) {
                try (Stream<Path> walk = Files.walk(myPath)) {
                    walk.filter(Files::isRegularFile).forEach(this::processPath); //只保留普通文件，排除目录
                }
            } else {
                logger.warn("Minio directory not found in JAR at {}", myPath);
            }
        }
    }

    //文件系统模式处理
    private void processFileSystemResources(org.springframework.core.io.Resource[] resources) throws IOException {
        for (org.springframework.core.io.Resource resource : resources) { //遍历所有扫描到的资源
            if (resource.isReadable() && resource.isFile()) { //检查是否可读且是文件（排除目录）
                processResource(resource); //处理单个文件
            }
        }
    }

    private void processPath(Path path) {
        //截掉 /BOOT-INF/classes/minio/ 前缀
        String objectName = path.toString().substring("/BOOT-INF/classes/minio/".length());
        try (InputStream inputStream = Files.newInputStream(path)) {
            uploadToMinio(objectName, inputStream, Files.size(path));
        } catch (IOException e) {
            logger.error("Error processing file: {}", objectName, e);
        }
    }

    private void processResource(org.springframework.core.io.Resource resource) {
        try {
            String path = resource.getURI().toString();
            //找到 "minio/" 的位置，截取它后面的部分作为对象名
            //把 \ 替换成 /（Windows 路径分隔符转成 MinIO 的标准格式）
            String objectName = path.substring(path.indexOf("minio/") + "minio/".length()).replace("\\", "/");
            try (InputStream inputStream = resource.getInputStream()) {
                uploadToMinio(objectName, inputStream, resource.contentLength());
            }
        } catch (Exception e) {
            logger.error("Error processing resource: {}", resource.getFilename(), e);
        }
    }

    private void uploadToMinio(String objectName, InputStream inputStream, long size) throws IOException {
        if (!minioUtil.isObjectExist(objectName)) { //检查 MinIO 中是否已经存在这个文件
            String contentType = determineContentType(objectName); //获取文件类型
            minioUtil.upload(inputStream, objectName, contentType, size);
            logger.info("Uploaded file: {}", objectName);
        } else {
            logger.info("File already exists: {}", objectName);
        }
    }

    //获取文件类型
    private String determineContentType(String filename) {
        String extension = getFileExtension(filename);
        return switch (extension.toLowerCase()) {
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf("."); //找到最后一个点号的位置
        if (lastIndexOf == -1) {
            return ""; //如果没有点号（lastIndexOf == -1），返回空字符串
        }
        //否则返回点号后面的部分作为扩展名
        return filename.substring(lastIndexOf + 1);
    }
}
