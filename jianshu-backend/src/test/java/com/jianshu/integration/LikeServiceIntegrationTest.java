package com.jianshu.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.entity.Article;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LikeService 集成测试
 * 启动 Spring 容器 + H2 内存数据库，测试 Service + Mapper + 数据库的完整交互。
 */
@SpringBootTest(classes = com.jianshu.JianshuApplication.class)
@ActiveProfiles("test")
@Transactional
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 测试点赞：zhangsan(user_id=1) 点赞 三国演义(article_id=1)
     * 初始 like_count=32，点赞后应变为 33
     */
    @Test
    void testToggleLikeAdd() {
        Long userId = 1L;
        Long articleId = 1L;

        Article before = articleMapper.selectById(articleId);
        int likeCountBefore = before.getLikeCount();

        Map<String, Object> result = likeService.toggleLike(userId, articleId);

        assertEquals(true, result.get("liked"));
        assertEquals(likeCountBefore + 1, result.get("likeCount"));

        // 验证数据库 like_count 已更新
        Article after = articleMapper.selectById(articleId);
        assertEquals(likeCountBefore + 1, after.getLikeCount());
    }

    /**
     * 测试取消点赞：lisi(user_id=2) 已点赞 三国演义(article_id=1)，再次调用应取消
     * 初始 like_count=32，取消后应变为 31
     */
    @Test
    void testToggleLikeCancel() {
        Long userId = 2L;
        Long articleId = 1L;

        Article before = articleMapper.selectById(articleId);
        int likeCountBefore = before.getLikeCount();

        Map<String, Object> result = likeService.toggleLike(userId, articleId);

        assertEquals(false, result.get("liked"));
        assertEquals(likeCountBefore - 1, result.get("likeCount"));

        // 验证数据库 like_count 已更新
        Article after = articleMapper.selectById(articleId);
        assertEquals(likeCountBefore - 1, after.getLikeCount());
    }

    /**
     * 测试点赞不存在的文章：抛 RuntimeException("文章不存在")
     */
    @Test
    void testToggleLikeArticleNotFound() {
        Long userId = 1L;
        Long articleId = 999L;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> likeService.toggleLike(userId, articleId));
        assertEquals("文章不存在", ex.getMessage());
    }

    /**
     * 测试获取我点赞的文章列表：zhangsan(user_id=1) 点赞了 红楼梦(article_id=2)
     */
    @Test
    void testGetMyLikedArticles() {
        Long userId = 1L;

        IPage<Map<String, Object>> page = likeService.getMyLikedArticles(userId, 1, 10);

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());

        Map<String, Object> article = page.getRecords().get(0);
        assertEquals(2L, article.get("id"));
        assertEquals("《红楼梦》", article.get("title"));

        // 应包含作者信息
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) article.get("author");
        assertNotNull(author);
        assertEquals("李四", author.get("nickname"));
    }
}
