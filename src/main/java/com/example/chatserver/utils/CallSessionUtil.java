package com.example.chatserver.utils;

import com.example.chatserver.exception.BaseException;

public class CallSessionUtil {
    private static final String PREFIX = "group_";

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
}
