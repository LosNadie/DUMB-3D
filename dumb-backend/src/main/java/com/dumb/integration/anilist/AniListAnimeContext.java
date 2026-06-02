package com.dumb.integration.anilist;

import lombok.Data;

import java.util.List;

/**
 * AniList 查询到的动漫元数据。
 */
@Data
public class AniListAnimeContext {

    /** 动漫标题（优先中文/原名） */
    private String title;

    /** 封面图 URL */
    private String coverImageUrl;

    /** 横幅/背景图 URL（可能为空） */
    private String bannerImageUrl;

    /** 主要动画制作公司 */
    private String studio;

    /** 风格标签（AniList 原始英文） */
    private List<String> genres;

    /** 首播日期，格式 YYYY-MM-DD */
    private String releaseDate;

    /** AniList 评分 0-100 */
    private Integer averageScore;

    /** AniList 简介（可能含 HTML） */
    private String description;
}
