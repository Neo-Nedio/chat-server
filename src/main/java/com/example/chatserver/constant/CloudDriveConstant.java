package com.example.chatserver.constant;

import java.util.HashMap;
import java.util.Map;

public class CloudDriveConstant {

    //文件分类：图片
    public static final String CATEGORY_IMAGE = "image";
    //文件分类：视频
    public static final String CATEGORY_VIDEO = "video";
    //文件分类：文档
    public static final String CATEGORY_DOCUMENT = "document";
    //文件分类：音频
    public static final String CATEGORY_AUDIO = "audio";
    //文件分类：压缩包
    public static final String CATEGORY_ARCHIVE = "archive";

    //用户空间默认容量 50G
    public static final long DEFAULT_USER_SPACE_QUOTA_BYTES = 50L * 1024 * 1024 * 1024;

    //回收站默认保留天数
    public static final int DEFAULT_RECYCLE_EXPIRE_DAYS = 30;

    //云盘文件在MinIO中的对象名前缀
    public static final String STORAGE_PREFIX = "clouddrive";

    //分片上传已传分片的Redis key：upload:chunk:文件hash
    public static final String REDIS_UPLOAD_CHUNK = "upload:chunk:%s";

    //分片记录过期时间（秒）
    public static final long UPLOAD_CHUNK_TTL_SECONDS = 60 * 60;

    //后缀与文件分类的映射关系，同时作为上传白名单（不在表内的后缀不允许上传）
    private static final Map<String, String> EXT_CATEGORY_MAP = new HashMap<>();

    static {
        //图片
        for (String ext : new String[]{"jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff"}) {
            EXT_CATEGORY_MAP.put(ext, CATEGORY_IMAGE);
        }
        //视频
        for (String ext : new String[]{"mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v", "3gp"}) {
            EXT_CATEGORY_MAP.put(ext, CATEGORY_VIDEO);
        }
        //音频
        for (String ext : new String[]{"mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus"}) {
            EXT_CATEGORY_MAP.put(ext, CATEGORY_AUDIO);
        }
        //文档
        for (String ext : new String[]{"doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "txt", "csv", "md", "rtf", "odt", "ods", "odp"}) {
            EXT_CATEGORY_MAP.put(ext, CATEGORY_DOCUMENT);
        }
        //压缩包
        for (String ext : new String[]{"zip", "rar", "7z", "tar", "gz", "bz2", "xz", "zst"}) {
            EXT_CATEGORY_MAP.put(ext, CATEGORY_ARCHIVE);
        }
    }

    /**
     * 是否允许上传的文件后缀（白名单校验，大小写不敏感）
     */
    public static boolean isAllowedUploadExt(String fileName) {
        String ext = fileExt(fileName);
        return !ext.isEmpty() && EXT_CATEGORY_MAP.containsKey(ext);
    }

    /**
     * 根据文件后缀推断文件分类，未匹配的归为空
     */
    public static String fileCategoryFromExt(String fileName) {
        String ext = fileExt(fileName);
        return EXT_CATEGORY_MAP.getOrDefault(ext, "");
    }

    /**
     * 取文件后缀（不含点、小写），如 photo.JPG -> jpg；无后缀返回空串
     */
    public static String fileExt(String fileName) {
        if (fileName == null) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
