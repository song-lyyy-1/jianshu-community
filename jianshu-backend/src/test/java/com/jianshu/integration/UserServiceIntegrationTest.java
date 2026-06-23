package com.jianshu.integration;

import com.jianshu.dto.LoginDTO;
import com.jianshu.dto.RegisterDTO;
import com.jianshu.entity.User;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UserService 集成测试
 * 启动 Spring 容器 + H2 内存数据库，测试 Service + Mapper + 数据库的完整交互。
 */
@SpringBootTest(classes = com.jianshu.JianshuApplication.class)
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 测试登录成功：使用 test123 登录 zhangsan，返回 token 和 user 信息
     */
    @Test
    void testLoginSuccess() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("zhangsan");
        loginDTO.setPassword("test123");

        Map<String, Object> result = userService.login(loginDTO);

        assertNotNull(result.get("token"));
        String token = (String) result.get("token");
        assertTrue(token.length() > 0, "token 不应为空");

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) result.get("user");
        assertNotNull(userMap);
        assertEquals(1L, userMap.get("id"));
        assertEquals("zhangsan", userMap.get("username"));
        assertEquals("张三", userMap.get("nickname"));
    }

    /**
     * 测试登录失败：密码错误抛 RuntimeException("密码错误")
     */
    @Test
    void testLoginWrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("zhangsan");
        loginDTO.setPassword("wrongpassword");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(loginDTO));
        assertEquals("密码错误", ex.getMessage());
    }

    /**
     * 测试登录失败：用户不存在抛 RuntimeException("用户不存在")
     */
    @Test
    void testLoginUserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nouser");
        loginDTO.setPassword("test123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(loginDTO));
        assertEquals("用户不存在", ex.getMessage());
    }

    /**
     * 测试注册成功：注册新用户，能在数据库中查到记录
     */
    @Test
    void testRegisterSuccess() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("newpass123");
        registerDTO.setNickname("新用户");

        Map<String, Object> result = userService.register(registerDTO);

        assertNotNull(result.get("token"));
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) result.get("user");
        assertNotNull(userMap);
        assertEquals("newuser", userMap.get("username"));
        assertEquals("新用户", userMap.get("nickname"));

        // 验证数据库中确实插入了该用户
        User dbUser = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "newuser")
        );
        assertNotNull(dbUser);
        assertEquals("新用户", dbUser.getNickname());
    }

    /**
     * 测试注册失败：重复用户名注册抛 RuntimeException("用户名已存在")
     */
    @Test
    void testRegisterDuplicateUsername() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("zhangsan");
        registerDTO.setPassword("test123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(registerDTO));
        assertEquals("用户名已存在", ex.getMessage());
    }

    /**
     * 测试获取用户统计数据：文章数、浏览量、点赞量、收藏量
     * zhangsan(user_id=1) 有 1 篇已发布文章（三国演义），浏览数256、点赞32、收藏18
     */
    @Test
    void testGetUserStats() {
        Map<String, Object> stats = userService.getUserStats(1L);

        assertNotNull(stats);
        assertEquals(1L, stats.get("articleCount"));
        assertEquals(256L, stats.get("totalViews"));
        assertEquals(32L, stats.get("totalLikes"));
        assertEquals(18L, stats.get("totalFavorites"));
    }

    /**
     * 测试更新用户信息：更新昵称和简介
     */
    @Test
    void testUpdateUserInfo() {
        Long userId = 1L;
        String newNickname = "新昵称";
        String newBio = "新的个人简介";

        User updatedUser = userService.updateUserInfo(userId, newNickname, null, newBio);

        assertNotNull(updatedUser);
        assertEquals(newNickname, updatedUser.getNickname());
        assertEquals(newBio, updatedUser.getBio());

        // 验证数据库中确实更新了
        User dbUser = userMapper.selectById(userId);
        assertEquals(newNickname, dbUser.getNickname());
        assertEquals(newBio, dbUser.getBio());
    }
}
