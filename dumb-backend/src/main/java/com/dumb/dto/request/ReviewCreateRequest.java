package com.dumb.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "正文不能为空")
    private String content;
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0.0", message = "评分最低0")
    @DecimalMax(value = "10.0", message = "评分最高10")
    private BigDecimal score;
    private String coverImage;
}
