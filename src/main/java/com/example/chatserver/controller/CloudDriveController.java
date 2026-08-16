package com.example.chatserver.controller;

import cn.hutool.json.JSONObject;
import com.example.chatserver.annotation.Userid;
import com.example.chatserver.exception.BaseException;
import com.example.chatserver.service.SpaceRecycleService;
import com.example.chatserver.service.SpaceService;
import com.example.chatserver.utils.ResultUtil;
import com.example.chatserver.vo.cloudDrive.FileDeleteVo;
import com.example.chatserver.vo.cloudDrive.RecycleDeleteVo;
import com.example.chatserver.vo.cloudDrive.RecycleRestoreVo;
import com.example.chatserver.vo.cloudDrive.UploadCheckVo;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * 个人云盘接口：仅本人可操作，文件平铺无目录，
 * 上传仅支持图片/视频/音频/文档/压缩包（白名单见 CloudDriveConstant）。
 */
@RestController
@RequestMapping("/v1/api/cloudDrive")
public class CloudDriveController {

    @Resource
    SpaceService spaceService;

    @Resource
    SpaceRecycleService spaceRecycleService;

    /**
     * 查询个人云盘信息（容量、已用，不存在则创建）
     */
    @PostMapping("/space/info")
    public JSONObject spaceInfo(@Userid String userId) {
        return ResultUtil.Succeed(spaceService.getOrCreateUserSpace(userId));
    }

    /**
     * 查询全部文件列表（按更新时间倒序）
     */
    @PostMapping("/file/list")
    public JSONObject fileList(@Userid String userId) {
        return ResultUtil.Succeed(spaceService.listUserFiles(userId));
    }

    /**
     * 删除文件（进入回收站，默认保留30天）
     */
    @PostMapping("/file/delete")
    public JSONObject fileDelete(@Userid String userId, @Valid @RequestBody FileDeleteVo fileDeleteVo) {
        spaceService.deleteUserFiles(userId, fileDeleteVo.getSpaceFileIds());
        return ResultUtil.Succeed();
    }

    /**
     * 查询各文件分类的数量与大小
     */
    @PostMapping("/file/categoryStats")
    public JSONObject fileCategoryStats(@Userid String userId) {
        return ResultUtil.Succeed(spaceService.listUserCategoryStats(userId));
    }

    /**
     * 获取文件下载/预览链接（MinIO临时访问URL，7天有效）
     */
    @GetMapping("/file/download")
    public JSONObject fileDownload(@Userid String userId, @RequestParam("spaceFileId") String spaceFileId) {
        return ResultUtil.Succeed(spaceService.getFileDownloadUrl(userId, spaceFileId));
    }

    /**
     * 查询回收站列表（过期记录不展示）
     */
    @PostMapping("/recycle/list")
    public JSONObject recycleList(@Userid String userId) {
        return ResultUtil.Succeed(spaceRecycleService.listUserRecycle(userId));
    }

    /**
     * 还原回收站文件（还原前校验空间容量）
     */
    @PostMapping("/recycle/restore")
    public JSONObject recycleRestore(@Userid String userId, @Valid @RequestBody RecycleRestoreVo recycleRestoreVo) {
        spaceRecycleService.restoreUserRecycle(userId, recycleRestoreVo.getSpaceRecycleIds());
        return ResultUtil.Succeed();
    }

    /**
     * 彻底删除回收站文件（不可恢复）
     */
    @PostMapping("/recycle/delete")
    public JSONObject recycleDelete(@Userid String userId, @Valid @RequestBody RecycleDeleteVo recycleDeleteVo) {
        spaceRecycleService.permanentlyDeleteUserRecycle(userId, recycleDeleteVo.getSpaceRecycleIds());
        return ResultUtil.Succeed();
    }

    /**
     * 清空回收站
     */
    @PostMapping("/recycle/clear")
    public JSONObject recycleClear(@Userid String userId) {
        spaceRecycleService.clearUserRecycle(userId);
        return ResultUtil.Succeed();
    }

    /**
     * 校验文件上传状态（类型白名单 + 秒传检查 + 已传分片）
     */
    @PostMapping("/upload/check")
    public JSONObject uploadCheck(@Userid String userId, @Valid @RequestBody UploadCheckVo uploadCheckVo) {
        return ResultUtil.Succeed(spaceService.checkUpload(userId, uploadCheckVo));
    }

    /**
     * 上传分片
     */
    @PostMapping("/upload/chunk")
    public JSONObject uploadChunk(@Userid String userId,
                                  @RequestParam("file") MultipartFile file,
                                  @RequestParam("fileHash") String fileHash,
                                  @RequestParam("chunkIndex") String chunkIndex) {
        if (file == null || file.isEmpty() || StringUtils.isBlank(fileHash) || StringUtils.isBlank(chunkIndex)) {
            throw new BaseException("参数错误");
        }
        spaceService.uploadChunk(file, fileHash, chunkIndex);
        return ResultUtil.Succeed();
    }

    /**
     * 合并分片
     */
    @PostMapping("/upload/merge")
    public JSONObject uploadMerge(@Userid String userId, @Valid @RequestBody UploadCheckVo uploadCheckVo) {
        return ResultUtil.Succeed(spaceService.uploadMerge(userId, uploadCheckVo));
    }
}
