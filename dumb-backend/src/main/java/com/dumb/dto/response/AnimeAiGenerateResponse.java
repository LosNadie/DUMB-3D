package com.dumb.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AnimeAiGenerateResponse {

    /**
     * 动漫名称
     */
    private String title;

    /**
     * 评论/简介正文（HTML 片段）
     */
    private String content;

    /**
     * 评分 0.0 - 10.0
     */
    private BigDecimal score;

    /**
     * 制片商/制作公司
     */
    private String studio;

    /**
     * 风格标签列表
     */
    private List<String> genres;

    /**
     * 发行日期，格式 YYYY-MM-DD
     */
    private String releaseDate;

    /**
     * 封面图片 URL
     */
    private String coverImage;

    /**
     * 背景图片 URL
     */
    private String backgroundImage;
}
