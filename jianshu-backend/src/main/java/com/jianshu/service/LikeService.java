package com.jianshu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.Map;

public interface LikeService {

    Map<String, Object> toggleLike(Long userId, Long articleId);

    IPage<Map<String, Object>> getMyLikedArticles(Long userId, int page, int size);
}