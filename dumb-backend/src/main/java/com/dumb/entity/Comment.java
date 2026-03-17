package com.dumb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String contentType;
    private Long contentId;
    private Long userId;
    private String content;
    private BigDecimal score;
    private Long parentId;
    @TableField(exist = false)
    private String username;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
