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
public class MovieCreateRequest {
    @NotBlank(message = "电影名称不能为空")
    private String title;
    @NotBlank(message = "简介不能为空")
    private String content;
    private String coverImage;
    @NotNull(message = "评分不能为空")
    @DecimalMin(value = "0.0", message = "评分最低0")
    @DecimalMax(value = "10.0", message = "评分最高10")
    private BigDecimal score;
    @NotBlank(message = "导演不能为空")
    private String director;
    @NotBlank(message = "主演不能为空")
    private String actors;
    private List<String> genres;
    @NotBlank(message = "电影地区不能为空")
    private String region;
    private String backgroundImage;
    private LocalDate releaseDate;
}
