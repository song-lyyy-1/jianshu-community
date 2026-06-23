package com.jianshu.unit;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.jianshu.dto.LoginDTO;
import com.jianshu.dto.RegisterDTO;
import com.jianshu.entity.Article;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.impl.UserServiceImpl;
import com.jianshu.util.JwtUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单元测试
 * 使用纯 Mockito mock 所有 Mapper 依赖，不启动 Spring 容器。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private ArticleLikeMapper articleLikeMapper;

    @Mock
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    /**
     * 初始化 MyBatis-Plus 实体的 Lambda 缓存。
     * 因为不启动 Spring 容器，LambdaUpdateWrapper.set() / LambdaQueryWrapper.select()
     * 会触发实体 lambda 缓存查找，需要预先初始化。
     */
    @BeforeAll
    static void initLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, Article.class);
    }

    /**
     * 测试登录成功：用户存在且密码正确，返回 token 和 user 信息
     */
    @Test
    void testLoginSuccess() {
        // 准备数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("test123");

        // 第一次 selectOne 返回用户基本信息（不含 password）
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("测试用户");
        user.setAvatar("avatar.png");
        user.setEmail("test@test.com");
        user.setBio("bio");
        user.setCreatedAt(LocalDateTime.now());

        // 第二次 selectOne 返回含密码的用户
        User userWithPassword = new User();
        userWithPassword.setId(1L);
        userWithPassword.setPassword(BCrypt.hashpw("test123"));

        when(userMapper.selectOne(any())).thenReturn(user, userWithPassword);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("mock-token");

        // 执行
        Map<String, Object> result = userService.login(loginDTO);

        // 验证
        assertEquals("mock-token", result.get("token"));
        assertNotNull(result.get("user"));
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) result.get("user");
        assertEquals(1L, userMap.get("id"));
        assertEquals("testuser", userMap.get("username"));
        assertEquals("测试用户", userMap.get("nickname"));
        verify(jwtUtil, times(1)).generateToken(1L, "testuser");
    }

    /**
     * 测试登录失败：用户不存在抛 RuntimeException("用户不存在")
     */
    @Test
    void testLoginUserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("nouser");
        loginDTO.setPassword("any");

        when(userMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(loginDTO));
        assertEquals("用户不存在", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyLong(), any());
    }

    /**
     * 测试登录失败：密码错误抛 RuntimeException("密码错误")
     */
    @Test
    void testLoginWrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpwd");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        User userWithPassword = new User();
        userWithPassword.setId(1L);
        userWithPassword.setPassword(BCrypt.hashpw("test123"));

        when(userMapper.selectOne(any())).thenReturn(user, userWithPassword);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.login(loginDTO));
        assertEquals("密码错误", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyLong(), any());
    }

    /**
     * 测试注册成功：用户名不存在，注册成功返回 token 和 user
     */
    @Test
    void testRegisterSuccess() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("new123");
        registerDTO.setNickname("新用户");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(jwtUtil.generateToken(anyLong(), any())).thenReturn("new-token");

        // 模拟 insert 后 id 被设置
        // 由于 userMapper.insert(user) 是 void 返回，但 MyBatis-Plus 会回填 id
        // 这里我们用 doAnswer 模拟回填 id
        org.mockito.Mockito.doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return 1;
        }).when(userMapper).insert(any(User.class));

        Map<String, Object> result = userService.register(registerDTO);

        assertEquals("new-token", result.get("token"));
        assertNotNull(result.get("user"));
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) result.get("user");
        assertEquals(10L, userMap.get("id"));
        assertEquals("newuser", userMap.get("username"));
        assertEquals("新用户", userMap.get("nickname"));
        verify(userMapper, times(1)).insert(any(User.class));
        verify(jwtUtil, times(1)).generateToken(10L, "newuser");
    }

    /**
     * 测试注册失败：用户名已存在抛 RuntimeException("用户名已存在")
     */
    @Test
    void testRegisterUsernameExists() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("exists");
        registerDTO.setPassword("any");

        when(userMapper.selectCount(any())).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(registerDTO));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    /**
     * 测试获取用户：调用 mapper.selectById 返回用户
     */
    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userMapper.selectById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userMapper, times(1)).selectById(1L);
    }

    /**
     * 测试获取用户统计信息：已发布文章数、浏览量、点赞量、收藏量
     */
    @Test
    void testGetUserStats() {
        Long userId = 1L;

        // 文章数
        when(articleMapper.selectCount(any())).thenReturn(2L);

        // 文章列表（用于统计浏览/点赞/收藏总数）
        Article a1 = new Article();
        a1.setViewCount(100);
        a1.setLikeCount(10);
        a1.setFavoriteCount(5);

        Article a2 = new Article();
        a2.setViewCount(200);
        a2.setLikeCount(20);
        a2.setFavoriteCount(15);

        when(articleMapper.selectList(any())).thenReturn(Arrays.asList(a1, a2));

        Map<String, Object> stats = userService.getUserStats(userId);

        assertEquals(2L, stats.get("articleCount"));
        assertEquals(300L, stats.get("totalViews"));
        assertEquals(30L, stats.get("totalLikes"));
        assertEquals(20L, stats.get("totalFavorites"));
    }

    /**
     * 测试更新用户信息：更新昵称、头像、简介
     */
    @Test
    void testUpdateUserInfo() {
        Long userId = 1L;
        String nickname = "新昵称";
        String avatar = "new-avatar.png";
        String bio = "新的简介";

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setNickname(nickname);
        updatedUser.setAvatar(avatar);
        updatedUser.setBio(bio);

        when(userMapper.update(any(), any())).thenReturn(1);
        when(userMapper.selectById(userId)).thenReturn(updatedUser);

        User result = userService.updateUserInfo(userId, nickname, avatar, bio);

        assertNotNull(result);
        assertEquals(nickname, result.getNickname());
        assertEquals(avatar, result.getAvatar());
        assertEquals(bio, result.getBio());
        verify(userMapper, times(1)).update(any(), any());
        verify(userMapper, times(1)).selectById(userId);
    }
}
