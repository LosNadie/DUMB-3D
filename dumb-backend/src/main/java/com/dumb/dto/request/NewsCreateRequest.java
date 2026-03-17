package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewsCreateRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "正文不能为空")
    private String content;
    private String coverImage;
    @NotBlank(message = "分类不能为空")
    private String category;
}
