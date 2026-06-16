package com.jianshu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jianshu.entity.Article;
import com.jianshu.entity.Comment;
import com.jianshu.entity.User;
import com.jianshu.mapper.ArticleMapper;
import com.jianshu.mapper.CommentMapper;
import com.jianshu.mapper.UserMapper;
import com.jianshu.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper, ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    public List<Map<String, Object>> getCommentList(Long articleId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByAsc(Comment::getCreatedAt)
        );

        return comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("articleId", comment.getArticleId());
            map.put("content", comment.getContent());
            map.put("createdAt", comment.getCreatedAt());

            // 评论者信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatar", user.getAvatar());
                map.put("user", userMap);
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Comment addComment(Long userId, Long articleId, String content) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        commentMapper.insert(comment);

        // 文章评论数+1
        articleMapper.update(null,
                new LambdaUpdateWrapper<Article>()
                        .eq(Article::getId, articleId)
                        .setSql("comment_count = comment_count + 1")
        );

        return comment;
    }
}