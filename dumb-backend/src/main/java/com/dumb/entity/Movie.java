package com.dumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("movie")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private Long authorId;
    private BigDecimal score;
    private String director;
    private String actors;
    private String genre;
    private String region;
    private String backgroundImage;
    private LocalDate releaseDate;
    private String status;
    private LocalDateTime publishTime;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
