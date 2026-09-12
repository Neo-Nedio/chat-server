package com.example.chatserver;

import com.example.chatserver.exception.BaseException;
import com.example.chatserver.utils.CallSessionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CallSessionUtilTest {
    @Test
    void createsAndParsesGroupSession() {
        assertEquals("group_g1001", CallSessionUtil.groupSession("g1001"));
        assertEquals("g1001", CallSessionUtil.parseGroupId("group_g1001"));
    }

    @Test
    void rejectsAmbiguousSession() {
        assertThrows(BaseException.class, () -> CallSessionUtil.groupSession("g_1001"));
        assertThrows(BaseException.class, () -> CallSessionUtil.parseGroupId("group_g_1001"));
        assertThrows(BaseException.class, () -> CallSessionUtil.parseGroupId("user_u1001"));
    }

    @Test
    void createsAndParsesLiveSession() {
        assertEquals("live_u1001", CallSessionUtil.liveSession("u1001"));
        assertEquals("u1001", CallSessionUtil.parseLiveUserId("live_u1001"));
        assertThrows(BaseException.class, () -> CallSessionUtil.liveSession("u_1001"));
        assertThrows(BaseException.class, () -> CallSessionUtil.parseLiveUserId("group_u1001"));
    }
}
