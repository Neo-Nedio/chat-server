package com.example.chatserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.chatserver.dto.CheckUploadResultDto;
import com.example.chatserver.dto.SpaceCategoryStatDto;
import com.example.chatserver.entity.Space;
import com.example.chatserver.entity.SpaceFile;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 个人云盘服务：仅本人可操作，文件平铺无目录，上传仅限白名单类型
 * （图片/视频/音频/文档/压缩包，见 CloudDriveConstant.EXT_CATEGORY_MAP）。
 */
public interface SpaceService extends IService<Space> {

    /**
     * 查询用户个人空间，不存在则创建（默认50G配额）
     */
    Space getOrCreateUserSpace(String userId);

    /**
     * 校验空间容量是否足够（quotaBytes为0表示不限制）
     */
    void checkSpaceQuota(Space space, long fileSize);

    /**
     * 查询用户全部文件（按更新时间倒序）
     */
    List<SpaceFile> listUserFiles(String userId);

    /**
     * 删除文件（直接物理删除，物理文件引用计数减1，配额扣减）
     */
    void deleteUserFiles(String userId, List<String> spaceFileIds);

    /**
     * 查询各文件分类的数量与大小（五类齐全，缺省补0）
     */
    List<SpaceCategoryStatDto> listUserCategoryStats(String userId);

    /**
     * 校验文件上传状态：类型白名单 + 配额校验，hash命中物理文件则秒传，否则返回已传分片（断点续传）
     */
    CheckUploadResultDto checkUpload(String userId, UploadCheckVo uploadCheckVo);

    /**
     * 上传单个分片（与空间无关，只依赖fileHash暂存）
     */
    void uploadChunk(MultipartFile file, String fileHash, String chunkIndex);

    /**
     * 合并分片：上传MinIO + 建物理文件 + 建逻辑文件，返回对象存储路径
     */
    String uploadMerge(String userId, UploadCheckVo uploadCheckVo);

    /**
     * 获取文件下载/预览链接（MinIO临时访问URL）
     */
    String getFileDownloadUrl(String userId, String spaceFileId);
}
