package com.dumb.integration.tmdb;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.config.TmdbProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TmdbClient {

    private final TmdbProperties tmdbProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TmdbClient(TmdbProperties tmdbProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.tmdbProperties = tmdbProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<TmdbMovieContext> findMovie(String search) {
        if (!StringUtils.hasText(tmdbProperties.getApiKey())) {
            log.warn("TMDB API Key 未配置，跳过 TMDB 查询");
            return Optional.empty();
        }
        if (!StringUtils.hasText(search)) {
            return Optional.empty();
        }

        try {
            // 1. 搜索电影
            String searchUrl = UriComponentsBuilder.fromHttpUrl(tmdbProperties.getBaseUrl() + "/search/movie")
                .queryParam("api_key", tmdbProperties.getApiKey())
                .queryParam("query", search.trim())
                .queryParam("language", "zh-CN")
                .toUriString();

            String searchResponse = restTemplate.getForObject(searchUrl, String.class);
            if (!StringUtils.hasText(searchResponse)) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(searchResponse);
            JsonNode results = root.path("results");
            if (!results.isArray() || results.isEmpty()) {
                return Optional.empty();
            }

            // 取第一个结果
            JsonNode first = results.get(0);
            int movieId = first.path("id").asInt(0);
            if (movieId <= 0) {
                return Optional.empty();
            }

            // 2. 获取详情
            return fetchMovieDetail(movieId);

        } catch (RestClientException e) {
            log.warn("调用 TMDB 失败: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("解析 TMDB 响应失败: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<TmdbMovieContext> fetchMovieDetail(int movieId) {
        try {
            String detailUrl = UriComponentsBuilder.fromHttpUrl(tmdbProperties.getBaseUrl() + "/movie/" + movieId)
                .queryParam("api_key", tmdbProperties.getApiKey())
                .queryParam("language", "zh-CN")
                .toUriString();

            String detailResponse = restTemplate.getForObject(detailUrl, String.class);
            if (!StringUtils.hasText(detailResponse)) {
                return Optional.empty();
            }

            JsonNode media = objectMapper.readTree(detailResponse);
            if (media.path("success").isBoolean() && !media.path("success").asBoolean()) {
                log.warn("TMDB 返回电影详情错误: id={}, status_message={}", movieId,
                    media.path("status_message").asText(""));
                return Optional.empty();
            }

            TmdbMovieContext ctx = new TmdbMovieContext();
            ctx.setId(movieId);
            ctx.setTitle(media.path("title").asText(""));
            ctx.setOverview(media.path("overview").asText(""));
            ctx.setPosterPath(media.path("poster_path").asText(""));
            ctx.setBackdropPath(media.path("backdrop_path").asText(""));
            ctx.setReleaseDate(parseDate(media.path("release_date")));

            // vote_average 是 0-10，保留一位小数
            JsonNode voteNode = media.path("vote_average");
            if (!voteNode.isMissingNode() && !voteNode.isNull()) {
                ctx.setVoteAverage(BigDecimal.valueOf(voteNode.asDouble(0))
                    .setScale(1, RoundingMode.HALF_UP));
            }

            // genres
            JsonNode genresNode = media.path("genres");
            List<String> genres = new ArrayList<>();
            if (genresNode.isArray()) {
                for (JsonNode g : genresNode) {
                    String name = g.path("name").asText("");
                    if (StringUtils.hasText(name)) {
                        genres.add(name);
                    }
                }
            }
            ctx.setGenres(genres);

            // production countries
            JsonNode countriesNode = media.path("production_countries");
            List<String> countries = new ArrayList<>();
            if (countriesNode.isArray()) {
                for (JsonNode c : countriesNode) {
                    String iso = c.path("iso_3166_1").asText("");
                    if (StringUtils.hasText(iso)) {
                        countries.add(iso);
                    }
                }
            }
            ctx.setProductionCountries(countries);

            log.info("TMDB 命中电影: id={}, title={}, voteAverage={}, genres={}",
                movieId, ctx.getTitle(), ctx.getVoteAverage(), ctx.getGenres());
            return Optional.of(ctx);

        } catch (Exception e) {
            log.warn("获取 TMDB 电影详情失败: id={}, reason={}", movieId, e.getMessage());
            return Optional.empty();
        }
    }

    private static String parseDate(JsonNode dateNode) {
        if (dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }
        String date = dateNode.asText("").trim();
        if (date.isEmpty() || "null".equals(date)) {
            return null;
        }
        return date;
    }

    public String buildImageUrl(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        String base = tmdbProperties.getImageBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String p = path.startsWith("/") ? path : "/" + path;
        return base + p;
    }
}
