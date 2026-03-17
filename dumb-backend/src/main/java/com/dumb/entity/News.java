package com.dumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news")
public class News {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private Long authorId;
    private String category;
    private String status;
    private LocalDateTime publishTime;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
