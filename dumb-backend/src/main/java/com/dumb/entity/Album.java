package com.dumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String artist;
    private String coverImage;
    private LocalDate releaseDate;
    private String genre;
    private String labelName;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
