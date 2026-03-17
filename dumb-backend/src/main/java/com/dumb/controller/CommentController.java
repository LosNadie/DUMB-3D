package com.dumb.controller;

import com.dumb.common.result.ApiResult;
import com.dumb.dto.request.CommentCreateRequest;
import com.dumb.entity.Comment;
import com.dumb.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController extends BaseController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ApiResult<List<Comment>> listByTarget(@RequestParam String contentType, @RequestParam Long contentId) {
        return ApiResult.success(commentService.listByTarget(contentType, contentId));
    }

    @PostMapping
    public ApiResult<Comment> create(@Valid @RequestBody CommentCreateRequest request) {
        return ApiResult.success(commentService.create(request, currentUsername()));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        commentService.deleteById(id, currentUsername());
        return ApiResult.success(null);
    }
}
