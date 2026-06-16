package com.jianshu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.dto.Result;
import com.jianshu.service.FavoriteService;
import com.jianshu.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/toggle/{articleId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        try {
            Map<String, Object> data = favoriteService.toggleFavorite(userId, articleId);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<IPage<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getUserId();
        IPage<Map<String, Object>> data = favoriteService.getMyFavoriteArticles(userId, page, size);
        return Result.success(data);
    }
}