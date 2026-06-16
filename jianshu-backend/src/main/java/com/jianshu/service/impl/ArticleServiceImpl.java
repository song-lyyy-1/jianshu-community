package com.jianshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianshu.dto.ArticleDTO;
import com.jianshu.entity.Article;
import com.jianshu.entity.ArticleFavorite;
import com.jianshu.entity.ArticleLike;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleFavoriteMapper;
import com.jianshu.mapper.ArticleLikeMapper;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper, UserMapper userMapper,
                              ArticleLikeMapper articleLikeMapper, ArticleFavoriteMapper articleFavoriteMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.articleLikeMapper = articleLikeMapper;
        this.articleFavoriteMapper = articleFavoriteMapper;
    }

    @Override
    public IPage<Map<String, Object>> getArticleList(int page, int size, String keyword) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Article::getTitle, keyword.trim());
        }
        queryWrapper.orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = articleMapper.selectPage(pageParam, queryWrapper);

        // 转换为包含作者信息的Map
        IPage<Map<String, Object>> resultPage = articlePage.convert(this::buildArticleMapWithAuthor);
        return resultPage;
    }

    @Override
    public Map<String, Object> getArticleDetail(Long id, Long currentUserId, boolean skipView) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 浏览数+1（编辑模式下不计数）
        if (!skipView) {
            articleMapper.update(null,
                    new LambdaUpdateWrapper<Article>().eq(Article::getId, id)
                            .setSql("view_count = view_count + 1")
            );
        }

        // 查询当前用户的点赞/收藏状态
        boolean liked = false;
        boolean favorited = false;
        if (currentUserId != null) {
            liked = articleLikeMapper.selectCount(
                    new LambdaQueryWrapper<ArticleLike>()
                            .eq(ArticleLike::getUserId, currentUserId)
                            .eq(ArticleLike::getArticleId, id)
            ) > 0;
            favorited = articleFavoriteMapper.selectCount(
                    new LambdaQueryWrapper<ArticleFavorite>()
                            .eq(ArticleFavorite::getUserId, currentUserId)
                            .eq(ArticleFavorite::getArticleId, id)
            ) > 0;
        }

        // 获取更新后的浏览数
        article = articleMapper.selectById(id);
        User author = userMapper.selectById(article.getUserId());

        Map<String, Object> detail = buildArticleMapWithAuthor(article);
        detail.put("liked", liked);
        detail.put("favorited", favorited);
        return detail;
    }

    @Override
    public Long createArticle(Long userId, ArticleDTO articleDTO) {
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setStatus(articleDTO.getStatus() != null ? articleDTO.getStatus() : 0);

        // summary自动生成
        String summary = generateSummary(articleDTO.getContent());
        article.setSummary(summary);

        articleMapper.insert(article);
        return article.getId();
    }

    @Override
    public Long updateArticle(Long userId, Long id, ArticleDTO articleDTO) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此文章");
        }

        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId, id);
        updateWrapper.set(Article::getTitle, articleDTO.getTitle());
        updateWrapper.set(Article::getContent, articleDTO.getContent());
        if (articleDTO.getStatus() != null) {
            updateWrapper.set(Article::getStatus, articleDTO.getStatus());
        }
        updateWrapper.set(Article::getSummary, generateSummary(articleDTO.getContent()));
        articleMapper.update(null, updateWrapper);

        return id;
    }

    @Override
    public void deleteArticle(Long userId, Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此文章");
        }
        articleMapper.deleteById(id);
    }

    @Override
    public IPage<Map<String, Object>> getMyArticles(Long userId, int page, int size, Integer status) {
        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(Article::getStatus, status);
        }
        queryWrapper.orderByDesc(Article::getCreatedAt);

        IPage<Article> articlePage = articleMapper.selectPage(pageParam, queryWrapper);
        return articlePage.convert(this::buildArticleMap);
    }

    private Map<String, Object> buildArticleMapWithAuthor(Article article) {
        Map<String, Object> map = buildArticleMap(article);
        User author = userMapper.selectById(article.getUserId());
        if (author != null) {
            Map<String, Object> authorMap = new HashMap<>();
            authorMap.put("id", author.getId());
            authorMap.put("nickname", author.getNickname());
            authorMap.put("avatar", author.getAvatar());
            map.put("author", authorMap);
        }
        return map;
    }

    private Map<String, Object> buildArticleMap(Article article) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", article.getId());
        map.put("userId", article.getUserId());
        map.put("title", article.getTitle());
        map.put("summary", article.getSummary());
        map.put("coverImage", article.getCoverImage());
        map.put("status", article.getStatus());
        map.put("viewCount", article.getViewCount());
        map.put("likeCount", article.getLikeCount());
        map.put("favoriteCount", article.getFavoriteCount());
        map.put("commentCount", article.getCommentCount());
        map.put("createdAt", article.getCreatedAt());
        map.put("updatedAt", article.getUpdatedAt());
        return map;
    }

    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        // 去除Markdown标记
        String plainText = content
                .replaceAll("#+\\s*", "")       // 去除标题标记
                .replaceAll("\\*{1,2}([^*]*)\\*{1,2}", "$1") // 去除加粗/斜体
                .replaceAll("`([^`]*)`", "$1")   // 去除行内代码
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1") // 去除链接，保留文字
                .replaceAll("!\\[([^\\]]*)\\]\\([^)]*\\)", "")  // 去除图片
                .replaceAll("---+", "")          // 去除分割线
                .replaceAll(">\\s*", "")         // 去除引用
                .replaceAll("-\\s+", "")         // 去除无序列表标记
                .replaceAll("\\d+\\.\\s+", "")   // 去除有序列表标记
                .replaceAll("\\n+", " ")         // 换行转为空格
                .trim();
        // 截取前100字符
        if (plainText.length() > 100) {
            return plainText.substring(0, 100);
        }
        return plainText;
    }
}