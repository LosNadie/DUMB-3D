package com.dumb.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnimeAiGenerateRequest {

    @NotBlank(message = "动漫名称不能为空")
    private String title;
}
