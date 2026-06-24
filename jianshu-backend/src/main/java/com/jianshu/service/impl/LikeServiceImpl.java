package com.jianshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleLike;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.LikeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LikeServiceImpl implements LikeService {

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public LikeServiceImpl(ArticleLikeMapper articleLikeMapper, ArticleMapper articleMapper, UserMapper userMapper) {
        this.articleLikeMapper = articleLikeMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> toggleLike(Long userId, Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        ArticleLike existing = articleLikeMapper.selectOne(
                new LambdaQueryWrapper<ArticleLike>()
                        .eq(ArticleLike::getUserId, userId)
                        .eq(ArticleLike::getArticleId, articleId)
        );

        boolean liked;
        int likeCount;

        if (existing != null) {
            // 已点赞 → 取消点赞
            articleLikeMapper.deleteById(existing.getId());
            articleMapper.update(null,
                    new LambdaUpdateWrapper<Article>().eq(Article::getId, articleId)
                            .setSql("like_count = like_count - 1")
            );
            liked = false;
        } else {
            // 未点赞 → 新增点赞
            ArticleLike articleLike = new ArticleLike();
            articleLike.setUserId(userId);
            articleLike.setArticleId(articleId);
            articleLikeMapper.insert(articleLike);
            articleMapper.update(null,
                    new LambdaUpdateWrapper<Article>().eq(Article::getId, articleId)
                            .setSql("like_count = like_count + 1")
            );
            liked = true;
        }

        // 获取最新likeCount
        article = articleMapper.selectById(articleId);
        likeCount = article.getLikeCount();

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Override
    public IPage<Map<String, Object>> getMyLikedArticles(Long userId, int page, int size) {
        // 先查询用户点赞的文章ID列表（分页）
        Page<ArticleLike> likePage = new Page<>(page, size);
        LambdaQueryWrapper<ArticleLike> likeQuery = new LambdaQueryWrapper<>();
        likeQuery.eq(ArticleLike::getUserId, userId)
                .orderByDesc(ArticleLike::getCreatedAt);
        IPage<ArticleLike> likeIPage = articleLikeMapper.selectPage(likePage, likeQuery);

        // 转换为文章信息Map，过滤掉文章已删除的null项
        IPage<Map<String, Object>> resultPage = likeIPage.convert(like -> {
            Article article = articleMapper.selectById(like.getArticleId());
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

            // 作者信息
            User author = userMapper.selectById(article.getUserId());
            if (author != null) {
                Map<String, Object> authorMap = new HashMap<>();
                authorMap.put("id", author.getId());
                authorMap.put("nickname", author.getNickname());
                authorMap.put("avatar", author.getAvatar());
                map.put("author", authorMap);
            }
            map.put("likedAt", like.getCreatedAt());
            return map;
        });

        // 过滤掉null元素（文章已被删除的情况）
        resultPage.setRecords(resultPage.getRecords().stream()
                .filter(item -> item != null)
                .collect(java.util.stream.Collectors.toList()));

        return resultPage;
    }
}