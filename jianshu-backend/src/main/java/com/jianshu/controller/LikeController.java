package com.jianshu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.dto.Result;
import com.jianshu.service.LikeService;
import com.jianshu.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/toggle/{articleId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long articleId) {
        Long userId = UserContext.getUserId();
        try {
            Map<String, Object> data = likeService.toggleLike(userId, articleId);
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
        IPage<Map<String, Object>> data = likeService.getMyLikedArticles(userId, page, size);
        return Result.success(data);
    }
}