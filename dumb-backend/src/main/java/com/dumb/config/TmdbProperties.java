package com.dumb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tmdb")
public class TmdbProperties {

    /**
     * TMDB API Key，从 https://www.themoviedb.org/settings/api 申请
     */
    private String apiKey;

    /**
     * TMDB API 根地址
     */
    private String baseUrl = "https://api.themoviedb.org/3";

    /**
     * 图片 CDN 基础地址
     */
    private String imageBaseUrl = "https://image.tmdb.org/t/p/original";
}
