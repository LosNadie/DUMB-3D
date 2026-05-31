package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReviewAiGenerateRequest {

    @NotBlank(message = "艺人不能为空")
    private String artist;

    @NotBlank(message = "专辑名称不能为空")
    private String albumTitle;

    @NotBlank(message = "年份不能为空")
    @Pattern(regexp = "^(19|20)\\d{2}$", message = "年份必须为 4 位数字，如 2025")
    private String year;
}
