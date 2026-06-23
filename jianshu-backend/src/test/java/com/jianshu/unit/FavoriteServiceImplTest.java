package com.jianshu.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleFavorite;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FavoriteServiceImpl 单元测试
 * 使用纯 Mockito mock 所有 Mapper 依赖，不启动 Spring 容器。
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    /**
     * 测试收藏：未收藏 → 新增收藏，favorite_count+1，返回 {favorited:true, favoriteCount:新值}
     */
    @Test
    void testToggleFavoriteAdd() {
        Long userId = 1L;
        Long articleId = 10L;

        Article article = new Article();
        article.setId(articleId);
        article.setFavoriteCount(5);

        Article updatedArticle = new Article();
        updatedArticle.setId(articleId);
        updatedArticle.setFavoriteCount(6);

        when(articleMapper.selectById(articleId)).thenReturn(article, updatedArticle);
        when(articleFavoriteMapper.selectOne(any())).thenReturn(null);
        when(articleFavoriteMapper.insert(any())).thenReturn(1);
        when(articleMapper.update(any(), any())).thenReturn(1);

        Map<String, Object> result = favoriteService.toggleFavorite(userId, articleId);

        assertEquals(true, result.get("favorited"));
        assertEquals(6, result.get("favoriteCount"));
        verify(articleFavoriteMapper, times(1)).insert(any(ArticleFavorite.class));
        verify(articleFavoriteMapper, never()).deleteById(anyLong());
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试取消收藏：已收藏 → 取消收藏，favorite_count-1，返回 {favorited:false, favoriteCount:新值}
     */
    @Test
    void testToggleFavoriteCancel() {
        Long userId = 1L;
        Long articleId = 10L;

        Article article = new Article();
        article.setId(articleId);
        article.setFavoriteCount(5);

        ArticleFavorite existing = new ArticleFavorite();
        existing.setId(88L);
        existing.setUserId(userId);
        existing.setArticleId(articleId);

        Article updatedArticle = new Article();
        updatedArticle.setId(articleId);
        updatedArticle.setFavoriteCount(4);

        when(articleMapper.selectById(articleId)).thenReturn(article, updatedArticle);
        when(articleFavoriteMapper.selectOne(any())).thenReturn(existing);
        when(articleFavoriteMapper.deleteById(88L)).thenReturn(1);
        when(articleMapper.update(any(), any())).thenReturn(1);

        Map<String, Object> result = favoriteService.toggleFavorite(userId, articleId);

        assertEquals(false, result.get("favorited"));
        assertEquals(4, result.get("favoriteCount"));
        verify(articleFavoriteMapper, times(1)).deleteById(88L);
        verify(articleFavoriteMapper, never()).insert(any(ArticleFavorite.class));
        verify(articleMapper, times(1)).update(any(), any());
    }

    /**
     * 测试收藏失败：文章不存在抛 RuntimeException
     */
    @Test
    void testToggleFavoriteArticleNotFound() {
        Long userId = 1L;
        Long articleId = 10L;

        when(articleMapper.selectById(articleId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> favoriteService.toggleFavorite(userId, articleId));
        assertEquals("文章不存在", ex.getMessage());
        verify(articleFavoriteMapper, never()).insert(any(ArticleFavorite.class));
        verify(articleFavoriteMapper, never()).deleteById(anyLong());
    }

    /**
     * 测试获取用户收藏的文章列表：包含作者信息和 favoritedAt
     */
    @Test
    void testGetMyFavoriteArticles() {
        Long userId = 1L;
        int page = 1;
        int size = 10;

        ArticleFavorite fav = new ArticleFavorite();
        fav.setId(1L);
        fav.setUserId(userId);
        fav.setArticleId(50L);
        fav.setCreatedAt(LocalDateTime.now());

        Page<ArticleFavorite> favPage = new Page<>(page, size);
        favPage.setRecords(List.of(fav));
        favPage.setTotal(1);

        Article article = new Article();
        article.setId(50L);
        article.setUserId(200L);
        article.setTitle("被收藏的文章");
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

        when(articleFavoriteMapper.selectPage(any(Page.class), any())).thenReturn(favPage);
        when(articleMapper.selectById(50L)).thenReturn(article);
        when(userMapper.selectById(200L)).thenReturn(author);

        IPage<Map<String, Object>> result = favoriteService.getMyFavoriteArticles(userId, page, size);

        assertEquals(1, result.getRecords().size());
        Map<String, Object> map = result.getRecords().get(0);
        assertEquals(50L, map.get("id"));
        assertEquals("被收藏的文章", map.get("title"));
        assertNotNull(map.get("author"));
        @SuppressWarnings("unchecked")
        Map<String, Object> authorMap = (Map<String, Object>) map.get("author");
        assertEquals(200L, authorMap.get("id"));
        assertEquals("作者", authorMap.get("nickname"));
        assertNotNull(map.get("favoritedAt"));
    }
}
