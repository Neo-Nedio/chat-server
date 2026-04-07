package com.example.chatserver.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.chatserver.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
//todo 学习minio
public class MinioUtil {
    @Resource
    private MinioConfig minioConfig;

    @Resource
    private MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     */
    public Boolean bucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient
                    .bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("检查桶是否存在失败, bucketName: {}", bucketName, e);
            return false;
        }
        return found;
    }

    /**
     * 创建存储bucket
     */
    public Boolean makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("创建桶失败, bucketName: {}", bucketName, e);
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     */
    public Boolean removeBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("删除桶失败, bucketName: {}", bucketName, e);
            return false;
        }
        return true;
    }

    /**
     * 获取全部bucket
     */
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            log.error("获取全部桶失败", e);
        }
        return null;
    }


    /**
     * 文件上传
     */
    public String upload(MultipartFile file) {
        // 1. 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException();  // 文件名为空，抛出异常
        }

        // 2. 生成新文件名（UUID + 原扩展名）
        String fileName = IdUtil.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 3. 构建对象路径（日期目录 + 文件名）
        // 文件名用UUID，不可能重复
        String objectName = DateUtil.today() + "/" + fileName;

        // 4. 上传到 MinIO
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())  // 存储桶
                    .object(objectName)                   // 对象路径
                    .stream(file.getInputStream(), file.getSize(), -1)  // 文件流
                    .contentType(file.getContentType())   // 内容类型
                    .build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return null;
        }

        // 5. 返回对象路径
        return objectName;
    }
    //上传 MultipartFile
    public String upload(MultipartFile file, String fileName) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException();
        }
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return null;
        }
        return minioConfig.getEndpoint() + "/" + fileName;
    }
    //上传 InputStream
    public String upload(InputStream in, String fileName, String type, long size) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(minioConfig.getBucketName()).object(fileName)
                    .stream(in, size, -1).contentType(type).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return null;
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + fileName;
    }
    public String uploadFile(InputStream in, String fileName, long size) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(minioConfig.getFileBucketName()).object(fileName)
                    .stream(in, size, -1).build();
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return null;
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getFileBucketName() + "/" + fileName;
    }

    public String getUrl(String fileName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getFileBucketName() + "/" + fileName;
    }


    /**
     * 预览图片
     * 生成文件临时访问链接，用于在前端预览图片或下载文件
     */
    public String preview(String fileName) {
        int expiry = 7 * 24 * 60 * 60; //临时访问链接的过期时间，用于控制生成的 URL 的有效时长
        // 查看文件地址
        try {
            GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                    .bucket(minioConfig.getBucketName()).object(fileName).expiry(expiry).method(Method.GET).build();
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            log.error("生成预览链接失败, fileName: {}", fileName, e);
        }
        return null;
    }

    /**
     * 预览文件
     */
    public String previewFile(String fileName) {
        int expiry = 7 * 24 * 60 * 60; //临时访问链接的过期时间，用于控制生成的 URL 的有效时长
        // 查看文件地址
        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder()
                .bucket(minioConfig.getFileBucketName()).object(fileName).expiry(expiry).method(Method.GET).build();
        try {
            return minioClient.getPresignedObjectUrl(build);
        } catch (Exception e) {
            log.error("生成预览链接失败, fileName: {}", fileName, e);
        }
        return null;
    }

    /**
     * 下载
     */
    public void download(String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(minioConfig.getBucketName())
                .object(fileName).build();
        //读取文件到内存
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            byte[] buf = new byte[1024]; // 1KB 缓冲区
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
                while ((len = response.read(buf)) != -1) {
                    os.write(buf, 0, len); // 循环写入内存
                }
                os.flush();
                byte[] bytes = os.toByteArray(); // 全部读完后转字节数组
                res.setCharacterEncoding("utf-8");
                // 设置强制下载不打开
                // res.setContentType("application/force-download");
                //设置响应头
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()) {
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            log.error("文件下载失败, fileName: {}", fileName, e);
        }
    }

    /**
     * 文件下载
     */
    public void downloadFile(String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(minioConfig.getFileBucketName())
                .object(fileName).build();
        try (GetObjectResponse response = minioClient.getObject(objectArgs)) {
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
                while ((len = response.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                // 设置强制下载不打开
                res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()) {
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            log.error("文件下载失败, fileName: {}", fileName, e);
        }
    }

    /**
     * 获取文件流
     */
    @SneakyThrows(Exception.class)
    public InputStream getObject(String objectName) {
        return minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getFileBucketName()).object(objectName).build());
    }

    /**
     * 查看文件对象
     */
    public List<Item> listObjects() {
       /* // Item 包含以下信息：
        item.objectName();        // 文件名/路径
        item.size();              // 文件大小（字节）
        item.lastModified();      // 最后修改时间
        item.etag();              // ETag（文件哈希）
        item.contentType();       // 内容类型
        item.isDir();             // 是否是目录*/
        // 1. 构建列表请求参数
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
        );

        // 2. 遍历结果并收集
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());  // Result.get() 可能抛出异常
            }
        } catch (Exception e) {
            log.error("查看文件对象失败", e);
            return null;
        }
        return items;
    }

    /**
     * 判断文件是否存在
     */
    public boolean isObjectExist(String objectName) {
        boolean exist = true;
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucketName()).object(objectName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }


    /**
     * 删除
     */
    public boolean remove(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName()).object(fileName).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 重命名
     */
    public void renameObject(String oldObjectName, String newObjectName) throws Exception {
        // 复制旧对象到新对象名
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(newObjectName)
                        .source(CopySource.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(oldObjectName)
                                .build())
                        .build()
        );
        // 删除旧对象
        remove(oldObjectName);
    }

}
