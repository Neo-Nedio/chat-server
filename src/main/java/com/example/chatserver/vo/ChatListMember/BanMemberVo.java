package com.example.chatserver.vo.ChatListMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class BanMemberVo {
    /**
     * 群ID
     */
    @NotBlank(message = "群ID不能为空")
    private String groupId;

    /**
     * 被禁言成员ID
     */
    @NotBlank(message = "成员ID不能为空")
    private String targetId;

    /**
     * 禁言时间（秒），如：3600表示禁言1小时，0表示解除禁言（不禁言）
     */
    @NotNull(message = "禁言时间不能为空")
    private Integer banDuration;  // 单位：秒

    /**
     * 获取禁言截止时间
     */
    public Date getBanEndTime() {
        if (banDuration == null) {
            return null;
        }
        if (banDuration == 0) {
            // 0表示解除禁言，返回null
            return null;
        }
        return new Date(System.currentTimeMillis() + banDuration * 1000L);// banDuration * 1000L 将秒转成毫秒
    }

    /**
     * 获取禁言提示文本
     */
    public String getBanMessage() {
        if (banDuration == 0) {
            return "已被解除禁言";
        }

        // 格式化时间
        if (banDuration < 60) {
            return "已被禁言 " + banDuration + " 秒";
        } else if (banDuration < 3600) {
            return "已被禁言 " + (banDuration / 60) + " 分钟";
        } else if (banDuration < 86400) {
            return "已被禁言 " + (banDuration / 3600) + " 小时";
        } else {
            return "已被禁言 " + (banDuration / 86400) + " 天";
        }
    }
}
