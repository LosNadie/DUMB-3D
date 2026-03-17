package com.dumb.service;

import com.dumb.dto.request.CommentCreateRequest;
import com.dumb.entity.Comment;

import java.util.List;

public interface CommentService {
    Comment create(CommentCreateRequest request, String username);
    List<Comment> listByTarget(String contentType, Long contentId);
    void deleteById(Long commentId, String username);
}
