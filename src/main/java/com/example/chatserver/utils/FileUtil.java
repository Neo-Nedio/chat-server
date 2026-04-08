package com.example.chatserver.utils;

import org.springframework.core.io.ByteArrayResource;

public class FileUtil {
    //用于将字节数组包装成带有文件名的 ByteArrayResource 对象。
    public static ByteArrayResource createByteArrayResource(byte[] content, String filename) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
