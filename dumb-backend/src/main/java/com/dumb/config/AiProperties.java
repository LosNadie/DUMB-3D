package com.dumb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * DeepSeek OpenAI 兼容接口根地址，默认官方
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * API Key，建议通过环境变量 AI_API_KEY 注入
     */
    private String apiKey;

    /**
     * 模型名，如 deepseek-chat
     */
    private String model = "deepseek-chat";
}
