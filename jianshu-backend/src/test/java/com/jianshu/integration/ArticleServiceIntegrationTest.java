package com.jianshu.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.dto.ArticleDTO;
import com.jianshu.entity.Article;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ArticleService 集成测试
 * 启动 Spring 容器 + H2 内存数据库，测试 Service + Mapper + 数据库的完整交互。
 */
@SpringBootTest(classes = com.jianshu.JianshuApplication.class)
@ActiveProfiles("test")
@Transactional
class ArticleServiceIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 测试获取已发布文章列表：只返回 status=1 的文章，不含草稿
     * data.sql 中有 2 篇已发布文章（三国演义、红楼梦）和 1 篇草稿
     */
    @Test
    void testGetArticleList() {
        IPage<Map<String, Object>> page = articleService.getArticleList(1, 10, null);

        assertEquals(2, page.getTotal());
        assertEquals(2, page.getRecords().size());

        // 不应包含草稿文章
        for (Map<String, Object> article : page.getRecords()) {
            Integer status = (Integer) article.get("status");
            assertEquals(1, status);
        }
    }

    /**
     * 测试按关键词搜索文章：搜索"三国"应只返回《三国演义》
     */
    @Test
    void testGetArticleListWithKeyword() {
        IPage<Map<String, Object>> page = articleService.getArticleList(1, 10, "三国");

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals("《三国演义》", page.getRecords().get(0).get("title"));
    }

    /**
     * 测试获取文章详情：浏览数+1，并返回作者信息和点赞/收藏状态
     */
    @Test
    void testGetArticleDetail() {
        Long articleId = 1L;
        Article before = articleMapper.selectById(articleId);
        int viewCountBefore = before.getViewCount();

        Map<String, Object> detail = articleService.getArticleDetail(articleId, null, false);

        assertNotNull(detail);
        assertEquals(articleId, detail.get("id"));
        assertEquals("《三国演义》", detail.get("title"));

        // 浏览数 +1
        Article after = articleMapper.selectById(articleId);
        assertEquals(viewCountBefore + 1, after.getViewCount());

        // 当前用户未登录，点赞/收藏状态应为 false
        assertEquals(false, detail.get("liked"));
        assertEquals(false, detail.get("favorited"));

        // 应包含作者信息
        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) detail.get("author");
        assertNotNull(author);
        assertEquals("张三", author.get("nickname"));
    }

    /**
     * 测试 skipView=true 时浏览数不增加
     */
    @Test
    void testGetArticleDetailSkipView() {
        Long articleId = 1L;
        Article before = articleMapper.selectById(articleId);
        int viewCountBefore = before.getViewCount();

        Map<String, Object> detail = articleService.getArticleDetail(articleId, null, true);

        assertNotNull(detail);
        // 浏览数不变
        Article after = articleMapper.selectById(articleId);
        assertEquals(viewCountBefore, after.getViewCount());
    }

    /**
     * 测试创建草稿文章：status=0，summary 自动生成
     */
    @Test
    void testCreateArticle() {
        Long userId = 1L;
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("测试草稿文章");
        dto.setContent("这是一篇用于测试的草稿文章内容，用于验证创建功能。");
        dto.setStatus(0);

        Long articleId = articleService.createArticle(userId, dto);

        assertNotNull(articleId);
        assertTrue(articleId > 0);

        Article dbArticle = articleMapper.selectById(articleId);
        assertNotNull(dbArticle);
        assertEquals("测试草稿文章", dbArticle.getTitle());
        assertEquals(0, dbArticle.getStatus());
        // summary 应自动生成
        assertNotNull(dbArticle.getSummary());
        assertFalse(dbArticle.getSummary().isEmpty());
    }

    /**
     * 测试创建并发布文章：status=1
     */
    @Test
    void testCreateArticlePublished() {
        Long userId = 1L;
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("测试发布文章");
        dto.setContent("这是一篇用于测试的已发布文章内容。");
        dto.setStatus(1);

        Long articleId = articleService.createArticle(userId, dto);

        assertNotNull(articleId);
        Article dbArticle = articleMapper.selectById(articleId);
        assertNotNull(dbArticle);
        assertEquals(1, dbArticle.getStatus());
        assertEquals(userId, dbArticle.getUserId());
    }

    /**
     * 测试更新文章内容：作者更新自己的文章
     */
    @Test
    void testUpdateArticle() {
        Long userId = 1L;
        Long articleId = 1L;

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("更新后的标题");
        dto.setContent("更新后的文章内容，用于验证更新功能。");

        Long updatedId = articleService.updateArticle(userId, articleId, dto);

        assertEquals(articleId, updatedId);

        Article dbArticle = articleMapper.selectById(articleId);
        assertEquals("更新后的标题", dbArticle.getTitle());
        assertEquals("更新后的文章内容，用于验证更新功能。", dbArticle.getContent());
    }

    /**
     * 测试非作者无法更新文章：lisi(user_id=2) 更新 zhangsan(user_id=1) 的文章应抛异常
     */
    @Test
    void testUpdateArticleNoPermission() {
        Long userId = 2L;
        Long articleId = 1L;

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("恶意修改");
        dto.setContent("恶意修改内容");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.updateArticle(userId, articleId, dto));
        assertEquals("无权限修改此文章", ex.getMessage());
    }

    /**
     * 测试删除文章：作者删除自己的文章
     */
    @Test
    void testDeleteArticle() {
        Long userId = 1L;
        Long articleId = 3L;

        articleService.deleteArticle(userId, articleId);

        Article dbArticle = articleMapper.selectById(articleId);
        assertNull(dbArticle);
    }

    /**
     * 测试非作者无法删除文章：lisi(user_id=2) 删除 zhangsan(user_id=1) 的文章应抛异常
     */
    @Test
    void testDeleteArticleNoPermission() {
        Long userId = 2L;
        Long articleId = 1L;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.deleteArticle(userId, articleId));
        assertEquals("无权限删除此文章", ex.getMessage());

        // 文章应仍存在
        Article dbArticle = articleMapper.selectById(articleId);
        assertNotNull(dbArticle);
    }

    /**
     * 测试获取我的所有文章（含草稿）：zhangsan(user_id=1) 有 2 篇文章（1 篇已发布 + 1 篇草稿）
     */
    @Test
    void testGetMyArticles() {
        Long userId = 1L;

        IPage<Map<String, Object>> page = articleService.getMyArticles(userId, 1, 10, null);

        assertEquals(2, page.getTotal());
        assertEquals(2, page.getRecords().size());
    }

    /**
     * 测试按状态筛选我的文章：zhangsan 已发布 1 篇、草稿 1 篇
     */
    @Test
    void testGetMyArticlesByStatus() {
        Long userId = 1L;

        // 已发布
        IPage<Map<String, Object>> publishedPage = articleService.getMyArticles(userId, 1, 10, 1);
        assertEquals(1, publishedPage.getTotal());
        assertEquals(1, publishedPage.getRecords().get(0).get("status"));

        // 草稿
        IPage<Map<String, Object>> draftPage = articleService.getMyArticles(userId, 1, 10, 0);
        assertEquals(1, draftPage.getTotal());
        assertEquals(0, draftPage.getRecords().get(0).get("status"));
    }
}
