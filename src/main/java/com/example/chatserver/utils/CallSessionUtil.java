package com.example.chatserver.utils;

import com.example.chatserver.exception.BaseException;

public class CallSessionUtil {
    private static final String PREFIX = "group_";
    private static final String LIVE_PREFIX = "live_";

    public static String groupSession(String groupId) {
        if (groupId == null || groupId.isBlank() || groupId.contains("_")) {
            throw new BaseException("群聊ID无效");
        }
        return PREFIX + groupId;
    }

    public static String parseGroupId(String sessionId) {
        if (sessionId == null || !sessionId.startsWith(PREFIX)
                || sessionId.length() == PREFIX.length()) {
            throw new BaseException("通话房间无效");
        }
        String groupId = sessionId.substring(PREFIX.length());
        if (groupId.contains("_") || groupId.isBlank()) {
            throw new BaseException("通话房间无效");
        }
        return groupId;
    }

    public static String liveSession(String userId) {
        if (userId == null || userId.isBlank() || userId.contains("_")) {
            throw new BaseException("用户ID无效");
        }
        return LIVE_PREFIX + userId;
    }

    public static String parseLiveUserId(String sessionId) {
        if (sessionId == null || !sessionId.startsWith(LIVE_PREFIX)
                || sessionId.length() == LIVE_PREFIX.length()) {
            throw new BaseException("直播房间无效");
        }
        String userId = sessionId.substring(LIVE_PREFIX.length());
        if (userId.contains("_") || userId.isBlank()) {
            throw new BaseException("直播房间无效");
        }
        return userId;
    }
}
