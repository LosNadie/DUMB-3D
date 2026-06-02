package com.dumb.integration.anilist;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class AniListClient {

    private static final String ANILIST_URL = "https://graphql.anilist.co";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AniListClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<AniListAnimeContext> findAnime(String search) {
        if (!StringUtils.hasText(search)) {
            return Optional.empty();
        }
        String query = ""
            + "query ($search: String) {"
            + "  Media(search: $search, type: ANIME) {"
            + "    title { native romaji english }"
            + "    coverImage { large extraLarge }"
            + "    bannerImage"
            + "    studios { nodes { name } }"
            + "    genres"
            + "    startDate { year month day }"
            + "    averageScore"
            + "    description"
            + "  }"
            + "}";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        body.put("variables", Map.of("search", search.trim()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(ANILIST_URL, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                String errMsg = errors.get(0).path("message").asText("AniList 查询失败");
                log.warn("AniList 返回错误: {}", errMsg);
                return Optional.empty();
            }

            JsonNode media = root.path("data").path("Media");
            if (media.isMissingNode() || media.isNull()) {
                return Optional.empty();
            }

            AniListAnimeContext ctx = new AniListAnimeContext();

            // title: 优先 native，其次 romaji，最后 english
            JsonNode titleNode = media.path("title");
            String nativeTitle = titleNode.path("native").asText("");
            String romajiTitle = titleNode.path("romaji").asText("");
            String englishTitle = titleNode.path("english").asText("");
            ctx.setTitle(StringUtils.hasText(nativeTitle) ? nativeTitle
                : (StringUtils.hasText(romajiTitle) ? romajiTitle : englishTitle));

            // cover image: 优先 extraLarge
            JsonNode coverNode = media.path("coverImage");
            String coverExtra = coverNode.path("extraLarge").asText("");
            String coverLarge = coverNode.path("large").asText("");
            ctx.setCoverImageUrl(StringUtils.hasText(coverExtra) ? coverExtra : coverLarge);

            // banner image
            ctx.setBannerImageUrl(media.path("bannerImage").asText(""));

            // studios: 取第一个
            JsonNode studiosNode = media.path("studios").path("nodes");
            if (studiosNode.isArray() && studiosNode.size() > 0) {
                ctx.setStudio(studiosNode.get(0).path("name").asText(""));
            }

            // genres
            JsonNode genresNode = media.path("genres");
            List<String> genres = new ArrayList<>();
            if (genresNode.isArray()) {
                for (JsonNode g : genresNode) {
                    if (g.isTextual()) {
                        genres.add(g.asText());
                    }
                }
            }
            ctx.setGenres(genres);

            // startDate → YYYY-MM-DD
            JsonNode dateNode = media.path("startDate");
            ctx.setReleaseDate(parseDate(dateNode));

            // averageScore (0-100)
            JsonNode scoreNode = media.path("averageScore");
            if (!scoreNode.isMissingNode() && !scoreNode.isNull()) {
                ctx.setAverageScore(scoreNode.asInt());
            }

            // description
            ctx.setDescription(media.path("description").asText(""));

            log.info("AniList 命中动漫: title={}, studio={}, score={}, genres={}",
                ctx.getTitle(), ctx.getStudio(), ctx.getAverageScore(), ctx.getGenres());
            return Optional.of(ctx);

        } catch (RestClientException e) {
            log.warn("调用 AniList 失败: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("解析 AniList 响应失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static String parseDate(JsonNode dateNode) {
        if (dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }
        Integer year = dateNode.has("year") && !dateNode.path("year").isNull()
            ? dateNode.path("year").asInt() : null;
        Integer month = dateNode.has("month") && !dateNode.path("month").isNull()
            ? dateNode.path("month").asInt() : null;
        Integer day = dateNode.has("day") && !dateNode.path("day").isNull()
            ? dateNode.path("day").asInt() : null;

        if (year == null || year <= 0) {
            return null;
        }
        int m = month != null && month > 0 ? month : 1;
        int d = day != null && day > 0 ? day : 1;
        return String.format("%04d-%02d-%02d", year, m, d);
    }
}
