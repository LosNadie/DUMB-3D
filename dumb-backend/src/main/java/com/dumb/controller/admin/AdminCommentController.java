package com.dumb.controller.admin;

import com.dumb.common.result.ApiResult;
import com.dumb.controller.BaseController;
import com.dumb.dto.request.CommentCreateRequest;
import com.dumb.entity.Comment;
import com.dumb.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController extends BaseController {
    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResult<Comment> create(@Valid @RequestBody CommentCreateRequest request) {
        return ApiResult.success(commentService.create(request, currentUsername()));
    }
}
