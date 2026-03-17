package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewCreateRequest {
    @NotBlank(message = "艺人名不能为空")
    private String artistName;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "正文不能为空")
    private String content;
    private String coverImage;
}
