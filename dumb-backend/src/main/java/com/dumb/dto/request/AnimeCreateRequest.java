package com.dumb.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AnimeCreateRequest {
    @NotBlank(message = "动漫名称不能为空")
    private String title;
    @NotBlank(message = "正文不能为空")
    private String content;
    private String coverImage;
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0.0", message = "评分最低0")
    @DecimalMax(value = "10.0", message = "评分最高10")
    private BigDecimal score;
    @NotBlank(message = "制片商不能为空")
    private String studio;
    private List<String> genres;
    private String backgroundImage;
    private LocalDate releaseDate;
}
