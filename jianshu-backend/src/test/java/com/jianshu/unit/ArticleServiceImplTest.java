package com.jianshu.unit;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.dto.ArticleDTO;
import com.jianshu.entity.Article;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.impl.ArticleServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * ArticleServiceImpl 单元测试
 * 使用纯 Mockito mock 所有 Mapper 依赖，不启动 Spring 容器。
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ArticleLikeMapper articleLikeMapper;

    @Mock
    private ArticleFavoriteMapper articleFavoriteMapper;

    @InjectMocks
    private ArticleServiceImpl articleService;

    /**
     * 初始化 MyBatis-Plus 实体的 Lambda 缓存。
     * 因为不启动 Spring 容器，LambdaUpdateWrapper.set() 会触发实体 lambda 缓存查找，
     * 需要预先初始化。
     */
    @BeforeAll
    static void initLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Article.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    /**
     * 构造一个完整的 Article 对象
     */
    private Article buildArticle(Long id, Long userId, String title, String content, Integer status,
                                 Integer viewCount, Integer likeCount, Integer favoriteCount) {
        Article article = new Article();
        article.setId(id);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setSummary("摘要");
        article.setCoverImage("cover.png");
        article.setStatus(status);
        article.setViewCount(viewCount);
        article.setLikeCount(likeCount);
        article.setFavoriteCount(favoriteCount);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        return article;
    }

    /**
     * 构造一个作者用户
     */
    private User buildAuthor(Long id, String nickname) {
        User author = new User();
        author.setId(id);
        author.setUsername("author" + id);
        author.setNickname(nickname);
        author.setAvatar("avatar" + id + ".png");
        return author;
    }

    /**
     * 测试获取文章列表：返回 status=1 的文章列表，包含作者信息
     */
    @Test
    void testGetArticleList() {
        int page = 1;
        int size = 10;

        Article a1 = buildArticle(1L, 100L, "文章1", "内容1", 1, 10, 2, 1);
        Article a2 = buildArticle(2L, 101L, "文章2", "内容2", 1, 20, 5, 3);

        Page<Article> articlePage = new Page<>(page, size);
        articlePage.setRecords(List.of(a1, a2));
        articlePage.setTotal(2);

        when(articleMapper.selectPage(any(Page.class), any())).thenReturn(articlePage);
        when(userMapper.selectById(100L)).thenReturn(buildAuthor(100L, "作者1"));
        when(userMapper.selectById(101L)).thenReturn(buildAuthor(101L, "作者2"));

        IPage<Map<String, Object>> result = articleService.getArticleList(page, size, null);

        assertEquals(2, result.getRecords().size());
        Map<String, Object> first = result.getRecords().get(0);
        assertEquals(1L, first.get("id"));
        assertEquals("文章1", first.get("title"));
        assertNotNull(first.get("author"));
        @SuppressWarnings("unchecked")
        Map<String, Object> authorMap1 = (Map<String, Object>) first.get("author");
        assertEquals(100L, authorMap1.get("id"));
        assertEquals("作者1", authorMap1.get("nickname"));
    }

    /**
     * 测试获取文章详情：浏览数+1，返回 liked/favorited 状态
     */
    @Test
    void testGetArticleDetail() {
        Long articleId = 1L;
        Long currentUserId = 100L;

        Article article = buildArticle(articleId, 200L, "文章", "内容", 1, 10, 2, 1);

        when(articleMapper.selectById(articleId)).thenReturn(article);
        when(articleLikeMapper.selectCount(any())).thenReturn(1L);
        when(articleFavoriteMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectById(200L)).thenReturn(buildAuthor(200L, "作者"));

        Map<String, Object> detail = articleService.getArticleDetail(articleId, currentUserId, false);

        assertEquals(articleId, detail.get("id"));
        assertEquals("文章", detail.get("title"));
        assertTrue((Boolean) detail.get("liked"));
        assertFalse((Boolean) detail.get("favorited"));
        // 浏览数+1 应该被调用
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试获取文章详情时 skipView=true，浏览数不增加
     */
    @Test
    void testGetArticleDetailSkipView() {
        Long articleId = 1L;
        Long currentUserId = 100L;

        Article article = buildArticle(articleId, 200L, "文章", "内容", 1, 10, 2, 1);

        when(articleMapper.selectById(articleId)).thenReturn(article);
        when(articleLikeMapper.selectCount(any())).thenReturn(0L);
        when(articleFavoriteMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.selectById(200L)).thenReturn(buildAuthor(200L, "作者"));

        Map<String, Object> detail = articleService.getArticleDetail(articleId, currentUserId, true);

        assertEquals(articleId, detail.get("id"));
        // skipView=true 时不应调用 update
        verify(articleMapper, never()).update(any(), any());
    }

    /**
     * 测试创建文章：status 默认 0，summary 自动生成
     */
    @Test
    void testCreateArticle() {
        Long userId = 1L;
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("新文章");
        dto.setContent("# 标题\n这是**加粗**的内容");

        // 模拟 insert 后 id 回填
        org.mockito.Mockito.doAnswer(invocation -> {
            Article a = invocation.getArgument(0);
            a.setId(100L);
            assertEquals(0, a.getStatus()); // status 默认为 0
            return 1;
        }).when(articleMapper).insert(any(Article.class));

        Long id = articleService.createArticle(userId, dto);

        assertEquals(100L, id);
        verify(articleMapper, times(1)).insert(any(Article.class));
    }

    /**
     * 测试创建文章：指定 status=1
     */
    @Test
    void testCreateArticleWithStatus() {
        Long userId = 1L;
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("发布文章");
        dto.setContent("内容");
        dto.setStatus(1);

        org.mockito.Mockito.doAnswer(invocation -> {
            Article a = invocation.getArgument(0);
            a.setId(101L);
            assertEquals(1, a.getStatus()); // status 应为 1
            return 1;
        }).when(articleMapper).insert(any(Article.class));

        Long id = articleService.createArticle(userId, dto);

        assertEquals(101L, id);
        verify(articleMapper, times(1)).insert(any(Article.class));
    }

    /**
     * 测试更新文章成功：作者本人更新成功
     */
    @Test
    void testUpdateArticleSuccess() {
        Long userId = 1L;
        Long articleId = 10L;

        Article existing = buildArticle(articleId, userId, "原标题", "原内容", 1, 0, 0, 0);
        when(articleMapper.selectById(articleId)).thenReturn(existing);
        when(articleMapper.update(any(), any())).thenReturn(1);

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("新标题");
        dto.setContent("新内容");

        Long result = articleService.updateArticle(userId, articleId, dto);

        assertEquals(articleId, result);
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试更新文章失败：非作者抛 RuntimeException("无权限修改此文章")
     */
    @Test
    void testUpdateArticleNoPermission() {
        Long userId = 1L;
        Long articleId = 10L;

        Article existing = buildArticle(articleId, 999L, "原标题", "原内容", 1, 0, 0, 0);
        when(articleMapper.selectById(articleId)).thenReturn(existing);

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("新标题");
        dto.setContent("新内容");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.updateArticle(userId, articleId, dto));
        assertEquals("无权限修改此文章", ex.getMessage());
        verify(articleMapper, never()).update(any(), any());
    }

    /**
     * 测试更新文章失败：文章不存在抛 RuntimeException("文章不存在")
     */
    @Test
    void testUpdateArticleNotFound() {
        Long userId = 1L;
        Long articleId = 10L;

        when(articleMapper.selectById(articleId)).thenReturn(null);

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("新标题");
        dto.setContent("新内容");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.updateArticle(userId, articleId, dto));
        assertEquals("文章不存在", ex.getMessage());
        verify(articleMapper, never()).update(any(), any());
    }

    /**
     * 测试删除文章成功：作者本人删除成功
     */
    @Test
    void testDeleteArticleSuccess() {
        Long userId = 1L;
        Long articleId = 10L;

        Article existing = buildArticle(articleId, userId, "标题", "内容", 1, 0, 0, 0);
        when(articleMapper.selectById(articleId)).thenReturn(existing);

        articleService.deleteArticle(userId, articleId);

        verify(articleMapper, times(1)).deleteById(articleId);
    }

    /**
     * 测试删除文章失败：非作者抛 RuntimeException
     */
    @Test
    void testDeleteArticleNoPermission() {
        Long userId = 1L;
        Long articleId = 10L;

        Article existing = buildArticle(articleId, 999L, "标题", "内容", 1, 0, 0, 0);
        when(articleMapper.selectById(articleId)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.deleteArticle(userId, articleId));
        assertEquals("无权限删除此文章", ex.getMessage());
        verify(articleMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试删除文章失败：文章不存在抛 RuntimeException
     */
    @Test
    void testDeleteArticleNotFound() {
        Long userId = 1L;
        Long articleId = 10L;

        when(articleMapper.selectById(articleId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.deleteArticle(userId, articleId));
        assertEquals("文章不存在", ex.getMessage());
        verify(articleMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试获取当前用户的文章列表
     */
    @Test
    void testGetMyArticles() {
        Long userId = 1L;
        int page = 1;
        int size = 10;

        Article a1 = buildArticle(1L, userId, "我的文章1", "内容1", 1, 5, 1, 0);
        Article a2 = buildArticle(2L, userId, "我的文章2", "内容2", 0, 0, 0, 0);

        Page<Article> articlePage = new Page<>(page, size);
        articlePage.setRecords(List.of(a1, a2));
        articlePage.setTotal(2);

        when(articleMapper.selectPage(any(Page.class), any())).thenReturn(articlePage);

        IPage<Map<String, Object>> result = articleService.getMyArticles(userId, page, size, null);

        assertEquals(2, result.getRecords().size());
        assertEquals(1L, result.getRecords().get(0).get("id"));
        assertEquals(userId, result.getRecords().get(0).get("userId"));
    }

    /**
     * 测试 Markdown 摘要生成（通过 createArticle 间接测试）
     */
    @Test
    void testGenerateSummary() {
        Long userId = 1L;
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("摘要测试");
        // 包含多种 Markdown 标记的内容
        dto.setContent("# 标题\n**加粗**内容`代码`[链接](http://x.com)\n- 列表项\n> 引用");

        org.mockito.Mockito.doAnswer(invocation -> {
            Article a = invocation.getArgument(0);
            String summary = a.getSummary();
            // 验证摘要已去除 Markdown 标记
            assertNotNull(summary);
            assertFalse(summary.contains("#"));
            assertFalse(summary.contains("**"));
            assertFalse(summary.contains("`"));
            assertFalse(summary.contains("["));
            assertFalse(summary.contains("]"));
            assertFalse(summary.contains(">"));
            assertFalse(summary.contains("-"));
            // 验证长度不超过 100
            assertTrue(summary.length() <= 100);
            return 1;
        }).when(articleMapper).insert(any(Article.class));

        articleService.createArticle(userId, dto);

        verify(articleMapper, times(1)).insert(any(Article.class));
    }
}
