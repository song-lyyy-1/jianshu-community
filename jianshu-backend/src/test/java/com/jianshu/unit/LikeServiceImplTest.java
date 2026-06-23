package com.jianshu.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleLike;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.impl.LikeServiceImpl;
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
 * LikeServiceImpl 单元测试
 * 使用纯 Mockito mock 所有 Mapper 依赖，不启动 Spring 容器。
 */
@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private ArticleLikeMapper articleLikeMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LikeServiceImpl likeService;

    /**
     * 测试点赞：未点赞 → 新增点赞，like_count+1，返回 {liked:true, likeCount:新值}
     */
    @Test
    void testToggleLikeAdd() {
        Long userId = 1L;
        Long articleId = 10L;

        Article article = new Article();
        article.setId(articleId);
        article.setLikeCount(5);

        // 第一次 selectById 返回原文章；第二次 selectById 返回更新后的文章
        Article updatedArticle = new Article();
        updatedArticle.setId(articleId);
        updatedArticle.setLikeCount(6);

        when(articleMapper.selectById(articleId)).thenReturn(article, updatedArticle);
        when(articleLikeMapper.selectOne(any())).thenReturn(null);
        when(articleLikeMapper.insert(any())).thenReturn(1);
        when(articleMapper.update(any(), any())).thenReturn(1);

        Map<String, Object> result = likeService.toggleLike(userId, articleId);

        assertEquals(true, result.get("liked"));
        assertEquals(6, result.get("likeCount"));
        verify(articleLikeMapper, times(1)).insert(any(ArticleLike.class));
        verify(articleLikeMapper, never()).deleteById(anyLong());
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试取消点赞：已点赞 → 取消点赞，like_count-1，返回 {liked:false, likeCount:新值}
     */
    @Test
    void testToggleLikeCancel() {
        Long userId = 1L;
        Long articleId = 10L;

        Article article = new Article();
        article.setId(articleId);
        article.setLikeCount(5);

        ArticleLike existing = new ArticleLike();
        existing.setId(99L);
        existing.setUserId(userId);
        existing.setArticleId(articleId);

        // 取消点赞后文章的 likeCount
        Article updatedArticle = new Article();
        updatedArticle.setId(articleId);
        updatedArticle.setLikeCount(4);

        when(articleMapper.selectById(articleId)).thenReturn(article, updatedArticle);
        when(articleLikeMapper.selectOne(any())).thenReturn(existing);
        when(articleLikeMapper.deleteById(99L)).thenReturn(1);
        when(articleMapper.update(any(), any())).thenReturn(1);

        Map<String, Object> result = likeService.toggleLike(userId, articleId);

        assertEquals(false, result.get("liked"));
        assertEquals(4, result.get("likeCount"));
        verify(articleLikeMapper, times(1)).deleteById(99L);
        verify(articleLikeMapper, never()).insert(any(ArticleLike.class));
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试点赞失败：文章不存在抛 RuntimeException
     */
    @Test
    void testToggleLikeArticleNotFound() {
        Long userId = 1L;
        Long articleId = 10L;

        when(articleMapper.selectById(articleId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> likeService.toggleLike(userId, articleId));
        assertEquals("文章不存在", ex.getMessage());
        verify(articleLikeMapper, never()).insert(any(ArticleLike.class));
        verify(articleLikeMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试获取用户点赞的文章列表：包含作者信息和 likedAt
     */
    @Test
    void testGetMyLikedArticles() {
        Long userId = 1L;
        int page = 1;
        int size = 10;

        ArticleLike like = new ArticleLike();
        like.setId(1L);
        like.setUserId(userId);
        like.setArticleId(50L);
        like.setCreatedAt(LocalDateTime.now());

        Page<ArticleLike> likePage = new Page<>(page, size);
        likePage.setRecords(List.of(like));
        likePage.setTotal(1);

        Article article = new Article();
        article.setId(50L);
        article.setUserId(200L);
        article.setTitle("被点赞的文章");
        article.setSummary("摘要");
        article.setCoverImage("cover.png");
        article.setStatus(1);
        article.setViewCount(10);
        article.setLikeCount(3);
        article.setFavoriteCount(2);
        article.setCreatedAt(LocalDateTime.now());

        User author = new User();
        author.setId(200L);
        author.setNickname("作者");
        author.setAvatar("avatar.png");

        when(articleLikeMapper.selectPage(any(Page.class), any())).thenReturn(likePage);
        when(articleMapper.selectById(50L)).thenReturn(article);
        when(userMapper.selectById(200L)).thenReturn(author);

        IPage<Map<String, Object>> result = likeService.getMyLikedArticles(userId, page, size);

        assertEquals(1, result.getRecords().size());
        Map<String, Object> map = result.getRecords().get(0);
        assertEquals(50L, map.get("id"));
        assertEquals("被点赞的文章", map.get("title"));
        assertNotNull(map.get("author"));
        @SuppressWarnings("unchecked")
        Map<String, Object> authorMap = (Map<String, Object>) map.get("author");
        assertEquals(200L, authorMap.get("id"));
        assertEquals("作者", authorMap.get("nickname"));
        assertNotNull(map.get("likedAt"));
    }
}
