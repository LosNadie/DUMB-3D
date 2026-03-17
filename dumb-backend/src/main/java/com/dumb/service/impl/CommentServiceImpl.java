package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ContentTypeEnum;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.enums.UserRoleEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.CommentCreateRequest;
import com.dumb.entity.Comment;
import com.dumb.entity.User;
import com.dumb.mapper.CommentMapper;
import com.dumb.mapper.UserMapper;
import com.dumb.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Comment create(CommentCreateRequest request, String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }

        boolean isReviewComment = ContentTypeEnum.REVIEW.getCode().equals(request.getContentType());
        boolean isTopLevelComment = request.getParentId() == null;

        if (isReviewComment && isTopLevelComment) {
            Comment existingReviewComment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getContentType, ContentTypeEnum.REVIEW.getCode())
                .eq(Comment::getContentId, request.getContentId())
                .eq(Comment::getUserId, user.getId())
                .isNull(Comment::getParentId)
                .last("LIMIT 1"));
            if (existingReviewComment != null) {
                existingReviewComment.setContent(request.getContent());
                // 同一用户同一乐评只保留一条评论，评分更新为最新值，不新增计分记录
                if (request.getScore() != null) {
                    existingReviewComment.setScore(request.getScore());
                }
                commentMapper.updateById(existingReviewComment);
                return existingReviewComment;
            }
        }

        Comment comment = new Comment();
        comment.setContentType(request.getContentType());
        comment.setContentId(request.getContentId());
        comment.setContent(request.getContent());
        if (isReviewComment && isTopLevelComment) {
            if (request.getScore() == null) {
                throw new BizException(ResultCodeEnum.PARAM_ERROR, "乐评评论必须包含评分");
            }
            comment.setScore(request.getScore());
        } else {
            comment.setScore(null);
        }
        comment.setParentId(request.getParentId());
        comment.setUserId(user.getId());
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public List<Comment> listByTarget(String contentType, Long contentId) {
        return commentMapper.selectListByTargetWithUsername(contentType, contentId);
    }

    @Override
    public void deleteById(Long commentId, String username) {
        if (username == null || username.trim().isEmpty() || "anonymousUser".equals(username)) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "请先登录");
        }
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCodeEnum.UNAUTHORIZED, "用户不存在");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BizException(ResultCodeEnum.NOT_FOUND, "评论不存在");
        }
        boolean isAdminLike = UserRoleEnum.ADMIN.getCode().equals(user.getRole())
            || UserRoleEnum.EDITOR.getCode().equals(user.getRole());
        boolean isOwner = user.getId().equals(comment.getUserId());
        if (!isAdminLike && !isOwner) {
            throw new BizException(ResultCodeEnum.FORBIDDEN, "仅可删除自己的评论");
        }
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
            .eq(Comment::getId, commentId)
            .or()
            .eq(Comment::getParentId, commentId));
    }
}
