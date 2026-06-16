package com.jianshu.controller;

import com.jianshu.dto.Result;
import com.jianshu.entity.Comment;
import com.jianshu.service.CommentService;
import com.jianshu.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/list/{articleId}")
    public Result<List<Map<String, Object>>> list(@PathVariable Long articleId) {
        List<Map<String, Object>> data = commentService.getCommentList(articleId);
        return Result.success(data);
    }

    @PostMapping("/add")
    public Result<Comment> add(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long articleId = Long.valueOf(params.get("articleId").toString());
        String content = (String) params.get("content");
        try {
            Comment comment = commentService.addComment(userId, articleId, content);
            return Result.success(comment);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}