package com.jianshu.service;

import com.jianshu.entity.Comment;
import java.util.List;
import java.util.Map;

public interface CommentService {

    List<Map<String, Object>> getCommentList(Long articleId);

    Comment addComment(Long userId, Long articleId, String content);
}