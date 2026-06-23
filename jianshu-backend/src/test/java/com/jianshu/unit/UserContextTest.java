package com.jianshu.unit;

import com.jianshu.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * UserContext 单元测试
 * 测试 ThreadLocal 用户上下文功能。
 */
class UserContextTest {

    @AfterEach
    void tearDown() {
        // 每个测试结束后清理 ThreadLocal，避免影响其他测试
        UserContext.clear();
    }

    /**
     * 测试设置后能获取 userId
     */
    @Test
    void testSetAndGetUserId() {
        Long userId = 123L;
        UserContext.setUserId(userId);
        assertEquals(userId, UserContext.getUserId());
    }

    /**
     * 测试设置后能获取 username
     */
    @Test
    void testSetAndGetUsername() {
        String username = "testuser";
        UserContext.setUsername(username);
        assertEquals(username, UserContext.getUsername());
    }

    /**
     * 测试 clear 后获取为 null
     */
    @Test
    void testClear() {
        UserContext.setUserId(1L);
        UserContext.setUsername("name");
        UserContext.clear();
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }

    /**
     * 测试初始值为 null
     */
    @Test
    void testInitialValues() {
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getUsername());
    }
}
