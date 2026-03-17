package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "内容类型不能为空")
    private String contentType;
    @NotNull(message = "内容ID不能为空")
    private Long contentId;
    @NotBlank(message = "评论内容不能为空")
    private String content;
    @DecimalMin(value = "0.0", message = "评分最低0")
    @DecimalMax(value = "10.0", message = "评分最高10")
    private BigDecimal score;
    private Long parentId;
}
