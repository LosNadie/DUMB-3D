package com.dumb.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReviewListItemVO {
    private Long id;
    private Long albumId;
    private String title;
    private String content;
    private String coverImage;
    private BigDecimal score;
    private String authorName;
    private String albumTitle;
    private String artist;
    private String genre;
    private LocalDate releaseDate;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
}
