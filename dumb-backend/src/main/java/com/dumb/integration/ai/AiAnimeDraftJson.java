package com.dumb.integration.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek 返回的动漫 JSON 对象结构。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnimeDraftJson {

    private String title;

    private String content;

    private String score;

    private String studio;

    private List<String> genres;

    private String releaseDate;

    private String coverImage;

    private String backgroundImage;
}
