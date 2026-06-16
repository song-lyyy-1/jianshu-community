package com.jianshu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.dto.ArticleDTO;
import com.jianshu.entity.Article;

import java.util.Map;

public interface ArticleService {

    IPage<Map<String, Object>> getArticleList(int page, int size, String keyword);

    Map<String, Object> getArticleDetail(Long id, Long currentUserId);

    Long createArticle(Long userId, ArticleDTO articleDTO);

    Long updateArticle(Long userId, Long id, ArticleDTO articleDTO);

    void deleteArticle(Long userId, Long id);

    IPage<Map<String, Object>> getMyArticles(Long userId, int page, int size, Integer status);
}