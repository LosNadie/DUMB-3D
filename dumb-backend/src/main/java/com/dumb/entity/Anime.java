package com.dumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("anime")
public class Anime {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private Long authorId;
    private BigDecimal score;
    private String studio;
    private String genre;
    private String backgroundImage;
    private LocalDate releaseDate;
    private String status;
    private LocalDateTime publishTime;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
