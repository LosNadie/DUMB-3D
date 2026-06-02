package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MovieAiGenerateRequest {

    @NotBlank(message = "电影名称不能为空")
    private String title;
}
