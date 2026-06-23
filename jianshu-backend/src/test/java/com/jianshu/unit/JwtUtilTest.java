package com.jianshu.unit;

import com.jianshu.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtUtil 单元测试
 * 由于 JwtUtil 使用 @Value 注入 secret 和 expiration，
 * 测试时通过反射设置这些字段值。
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "jianshu-community-secret-key-2024-must-be-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // 通过反射设置 @Value 注入的字段
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, SECRET);

        Field expirationField = JwtUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtil, EXPIRATION);
    }

    /**
     * 测试生成 token 后能正确解析出 username 和 userId
     */
    @Test
    void testGenerateAndParseToken() {
        Long userId = 100L;
        String username = "testuser";

        String token = jwtUtil.generateToken(userId, username);
        Claims claims = jwtUtil.parseToken(token);

        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
    }

    /**
     * 测试解析无效 token 抛异常
     */
    @Test
    void testParseInvalidToken() {
        String invalidToken = "this.is.an.invalid.token";
        assertThrows(Exception.class, () -> jwtUtil.parseToken(invalidToken));
    }

    /**
     * 测试未过期的 token 返回 false
     */
    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken(1L, "user");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    /**
     * 测试正确获取 userId
     */
    @Test
    void testGetUserIdFromToken() {
        Long userId = 200L;
        String token = jwtUtil.generateToken(userId, "alice");
        assertEquals(userId, jwtUtil.getUserIdFromToken(token));
    }

    /**
     * 测试正确获取 username
     */
    @Test
    void testGetUsernameFromToken() {
        String username = "bob";
        String token = jwtUtil.generateToken(1L, username);
        assertEquals(username, jwtUtil.getUsernameFromToken(token));
    }
}
