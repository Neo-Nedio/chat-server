package com.example.chatserver.utils;

import com.example.chatserver.constant.CloudDriveConstant;
import com.example.chatserver.exception.BaseException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


//云盘分片上传：分片本地暂存 + Redis记录已传分片（断点续传），合并后整体上传MinIO
@Component
@Slf4j
public class ChunkUploadUtil {

    //分片暂存目录
    private static final String CHUNK_DIR = "temp/chunks/";

    @Resource
    RedisUtils redisUtils;

    @Resource
    MinioUtil minioUtil;

    /**
     * 获取已上传的分片序号
     */
    public List<String> getUploadedChunks(String fileHash) {
        Set<Object> members = redisUtils.sGet(chunkKey(fileHash));
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        //排序保证断点续传时前端能确定缺失分片
        return members.stream().map(String::valueOf).sorted().collect(Collectors.toList());
    }

    /**
     * 上传单个分片：本地暂存（已存在则跳过写入），并记录到Redis
     */
    public void uploadChunk(MultipartFile file, String fileHash, String chunkIndex) throws IOException {
        //构造本地存储路径
        Path dir = Paths.get(CHUNK_DIR, fileHash);
        Files.createDirectories(dir);
        Path part = dir.resolve(chunkIndex + ".part");
        //保存分片文件（仅当文件不存在时）
        if (!Files.exists(part)) {
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        //在 Redis 中标记分片已上传
        redisUtils.sSetAndTime(chunkKey(fileHash), CloudDriveConstant.UPLOAD_CHUNK_TTL_SECONDS, chunkIndex);
    }

    /**
     * 合并分片并上传MinIO，返回对象名（storagePath）
     */
    public String mergeChunks(String fileHash, int totalChunk, String fileName) {
        if (totalChunk <= 0) {
            throw new BaseException("参数错误");
        }
        Path dir = Paths.get(CHUNK_DIR, fileHash);
        List<Path> parts = new ArrayList<>(totalChunk);
        long totalSize = 0;
        for (int i = 0; i < totalChunk; i++) {
            Path part = dir.resolve(i + ".part");
            if (!Files.exists(part)) {
                throw new BaseException("分片未上传完整，请补充缺失分片");
            }
            parts.add(part);
            totalSize += part.toFile().length();
        }

        //同hash对象天然只存一份，与物理文件表的hash唯一键对齐
        String objectName = CloudDriveConstant.STORAGE_PREFIX + "/" + fileHash + ext(fileName);
        List<FileInputStream> streams = new ArrayList<>(totalChunk);
        try {
            for (Path part : parts) {
                streams.add(new FileInputStream(part.toFile()));
            }
            //顺序拼接分片流，避免大文件整体读入内存
            SequenceInputStream sequence = new SequenceInputStream(Collections.enumeration(streams));
            String url = minioUtil.uploadFile(sequence, objectName, totalSize);
            if (url == null) {
                throw new BaseException("文件合并上传失败");
            }
        } catch (IOException e) {
            log.error("合并分片失败, fileHash: {}", fileHash, e);
            throw new BaseException("文件合并上传失败");
        } finally {
            for (FileInputStream stream : streams) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }

        //清理临时分片与Redis记录
        try {
            FileUtils.deleteDirectory(dir.toFile());
        } catch (IOException e) {
            log.error("清理分片临时目录失败, fileHash: {}", fileHash, e);
        }
        redisUtils.del(chunkKey(fileHash));
        return objectName;
    }

    private String chunkKey(String fileHash) {
        return String.format(CloudDriveConstant.REDIS_UPLOAD_CHUNK, fileHash);
    }

    /**
     * 取文件后缀（含点），如 .pdf；无后缀返回空串
     */
    private String ext(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index);
    }
}
