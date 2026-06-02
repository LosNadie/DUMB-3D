package com.dumb.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MovieAiGenerateResponse {

    private String title;

    private String content;

    private BigDecimal score;

    private String director;

    private String actors;

    private List<String> genres;

    private String region;

    private String releaseDate;

    private String coverImage;

    private String backgroundImage;
}
