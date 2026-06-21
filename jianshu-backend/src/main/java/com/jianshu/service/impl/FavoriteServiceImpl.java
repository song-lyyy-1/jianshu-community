package com.jianshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleFavorite;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final ArticleFavoriteMapper articleFavoriteMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public FavoriteServiceImpl(ArticleFavoriteMapper articleFavoriteMapper, ArticleMapper articleMapper, UserMapper userMapper) {
        this.articleFavoriteMapper = articleFavoriteMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> toggleFavorite(Long userId, Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        ArticleFavorite existing = articleFavoriteMapper.selectOne(
                new LambdaQueryWrapper<ArticleFavorite>()
                        .eq(ArticleFavorite::getUserId, userId)
                        .eq(ArticleFavorite::getArticleId, articleId)
        );

        boolean favorited;
        int favoriteCount;

        if (existing != null) {
            // 已收藏 → 取消收藏
            articleFavoriteMapper.deleteById(existing.getId());
            articleMapper.update(null,
                    new LambdaUpdateWrapper<Article>().eq(Article::getId, articleId)
                            .setSql("favorite_count = favorite_count - 1")
            );
            favorited = false;
        } else {
            // 未收藏 → 新增收藏
            ArticleFavorite articleFavorite = new ArticleFavorite();
            articleFavorite.setUserId(userId);
            articleFavorite.setArticleId(articleId);
            articleFavoriteMapper.insert(articleFavorite);
            articleMapper.update(null,
                    new LambdaUpdateWrapper<Article>().eq(Article::getId, articleId)
                            .setSql("favorite_count = favorite_count + 1")
            );
            favorited = true;
        }

        // 获取最新favoriteCount
        article = articleMapper.selectById(articleId);
        favoriteCount = article.getFavoriteCount();

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("favoriteCount", favoriteCount);
        return result;
    }

    @Override
    public IPage<Map<String, Object>> getMyFavoriteArticles(Long userId, int page, int size) {
        Page<ArticleFavorite> favPage = new Page<>(page, size);
        LambdaQueryWrapper<ArticleFavorite> favQuery = new LambdaQueryWrapper<>();
        favQuery.eq(ArticleFavorite::getUserId, userId)
                .orderByDesc(ArticleFavorite::getCreatedAt);
        IPage<ArticleFavorite> favIPage = articleFavoriteMapper.selectPage(favPage, favQuery);

        IPage<Map<String, Object>> resultPage = favIPage.convert(fav -> {
            Article article = articleMapper.selectById(fav.getArticleId());
            if (article == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", article.getId());
            map.put("title", article.getTitle());
            map.put("summary", article.getSummary());
            map.put("coverImage", article.getCoverImage());
            map.put("status", article.getStatus());
            map.put("viewCount", article.getViewCount());
            map.put("likeCount", article.getLikeCount());
            map.put("favoriteCount", article.getFavoriteCount());
            map.put("createdAt", article.getCreatedAt());

            User author = userMapper.selectById(article.getUserId());
            if (author != null) {
                Map<String, Object> authorMap = new HashMap<>();
                authorMap.put("id", author.getId());
                authorMap.put("nickname", author.getNickname());
                authorMap.put("avatar", author.getAvatar());
                map.put("author", authorMap);
            }
            map.put("favoritedAt", fav.getCreatedAt());
            return map;
        });

        return resultPage;
    }
}