package com.jianshu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Map;

public interface FavoriteService {

    Map<String, Object> toggleFavorite(Long userId, Long articleId);

    IPage<Map<String, Object>> getMyFavoriteArticles(Long userId, int page, int size);
}