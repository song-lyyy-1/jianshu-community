package com.jianshu.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.entity.Article;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * FavoriteService 集成测试
 * 启动 Spring 容器 + H2 内存数据库，测试 Service + Mapper + 数据库的完整交互。
 */
@SpringBootTest(classes = com.jianshu.JianshuApplication.class)
@ActiveProfiles("test")
@Transactional
class FavoriteServiceIntegrationTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 测试收藏：zhangsan(user_id=1) 收藏 三国演义(article_id=1)
     * 初始 favorite_count=18，收藏后应变为 19
     */
    @Test
    void testToggleFavoriteAdd() {
        Long userId = 1L;
        Long articleId = 1L;

        Article before = articleMapper.selectById(articleId);
        int favoriteCountBefore = before.getFavoriteCount();

        Map<String, Object> result = favoriteService.toggleFavorite(userId, articleId);

        assertEquals(true, result.get("favorited"));
        assertEquals(favoriteCountBefore + 1, result.get("favoriteCount"));

        // 验证数据库 favorite_count 已更新
        Article after = articleMapper.selectById(articleId);
        assertEquals(favoriteCountBefore + 1, after.getFavoriteCount());
    }

    /**
     * 测试取消收藏：lisi(user_id=2) 已收藏 三国演义(article_id=1)，再次调用应取消
     * 初始 favorite_count=18，取消后应变为 17
     */
    @Test
    void testToggleFavoriteCancel() {
        Long userId = 2L;
        Long articleId = 1L;

        Article before = articleMapper.selectById(articleId);
        int favoriteCountBefore = before.getFavoriteCount();

        Map<String, Object> result = favoriteService.toggleFavorite(userId, articleId);

        assertEquals(false, result.get("favorited"));
        assertEquals(favoriteCountBefore - 1, result.get("favoriteCount"));

        // 验证数据库 favorite_count 已更新
        Article after = articleMapper.selectById(articleId);
        assertEquals(favoriteCountBefore - 1, after.getFavoriteCount());
    }

    /**
     * 测试收藏不存在的文章：抛 RuntimeException("文章不存在")
     */
    @Test
    void testToggleFavoriteArticleNotFound() {
        Long userId = 1L;
        Long articleId = 999L;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.toggleFavorite(userId, articleId));
        assertEquals("文章不存在", ex.getMessage());
    }

    /**
     * 测试获取我收藏的文章列表：zhangsan(user_id=1) 收藏了 红楼梦(article_id=2)
     */
    @Test
    void testGetMyFavoriteArticles() {
        Long userId = 1L;

        IPage<Map<String, Object>> page = favoriteService.getMyFavoriteArticles(userId, 1, 10);

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
