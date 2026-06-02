package com.dumb.integration.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiMovieDraftJson {

    private String title;

    private String content;

    private String score;

    private String director;

    private String actors;

    private List<String> genres;

    private String region;

    private String releaseDate;

    private String coverImage;

    private String backgroundImage;
}
