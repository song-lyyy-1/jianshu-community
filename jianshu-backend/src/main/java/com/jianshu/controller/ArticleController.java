package com.jianshu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jianshu.dto.ArticleDTO;
import com.jianshu.dto.Result;
import com.jianshu.service.ArticleService;
import com.jianshu.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/list")
    public Result<IPage<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Map<String, Object>> data = articleService.getArticleList(page, size, keyword);
        return Result.success(data);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id,
                                               @RequestParam(required = false, defaultValue = "false") boolean skipView) {
        Long currentUserId = UserContext.getUserId();
        try {
            Map<String, Object> data = articleService.getArticleDetail(id, currentUserId, skipView);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ArticleDTO articleDTO) {
        Long userId = UserContext.getUserId();
        try {
            Long articleId = articleService.createArticle(userId, articleDTO);
            return Result.success(articleId);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public Result<Long> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDTO) {
        Long userId = UserContext.getUserId();
        try {
            Long updatedId = articleService.updateArticle(userId, id, articleDTO);
            return Result.success(updatedId);
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        try {
            articleService.deleteArticle(userId, id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<IPage<Map<String, Object>>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        IPage<Map<String, Object>> data = articleService.getMyArticles(userId, page, size, status);
        return Result.success(data);
    }
}