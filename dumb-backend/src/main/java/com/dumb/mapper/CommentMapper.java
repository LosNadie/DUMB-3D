package com.dumb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dumb.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    BigDecimal selectAverageScoreByTarget(@Param("contentType") String contentType, @Param("contentId") Long contentId);
    List<Comment> selectListByTargetWithUsername(@Param("contentType") String contentType, @Param("contentId") Long contentId);
}
