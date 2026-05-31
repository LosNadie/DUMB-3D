package com.dumb.integration.ai;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekClient {

    private static final int MAX_DESCRIPTION_CHARS = 50_000;

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(AiProperties aiProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AiDraftJson completeDraft(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "未配置 AI API，请在环境变量 AI_API_KEY 或配置项 ai.api-key 中设置");
        }
        String base = aiProperties.getBaseUrl() != null ? aiProperties.getBaseUrl().trim() : "";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String url = base + "/v1/chat/completions";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        body.put("response_format", Map.of("type", "json_object"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                throw new BizException(ResultCodeEnum.SERVER_ERROR, "AI 返回内容为空");
            }
            String cleaned = stripMarkdownFence(content);
            AiDraftJson draft = objectMapper.readValue(cleaned, AiDraftJson.class);
            if (draft.getDescription() != null && draft.getDescription().length() > MAX_DESCRIPTION_CHARS) {
                draft.setDescription(draft.getDescription().substring(0, MAX_DESCRIPTION_CHARS));
            }
            return draft;
        } catch (BizException e) {
            throw e;
        } catch (RestClientException e) {
            throw new BizException(ResultCodeEnum.SERVER_ERROR, "调用 AI 服务失败：" + e.getMessage());
        } catch (Exception e) {
            throw new BizException(ResultCodeEnum.SERVER_ERROR, "解析 AI 返回失败：" + e.getMessage());
        }
    }

    private static String stripMarkdownFence(String raw) {
        String s = raw.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl > 0) {
                s = s.substring(firstNl + 1);
            }
            int end = s.lastIndexOf("```");
            if (end > 0) {
                s = s.substring(0, end).trim();
            }
        }
        return s;
    }
}
