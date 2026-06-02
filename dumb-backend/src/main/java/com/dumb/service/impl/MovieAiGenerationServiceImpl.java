package com.dumb.service.impl;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.MovieAiGenerateRequest;
import com.dumb.dto.response.MovieAiGenerateResponse;
import com.dumb.integration.ai.AiMovieDraftJson;
import com.dumb.integration.ai.DeepSeekClient;
import com.dumb.integration.tmdb.TmdbClient;
import com.dumb.integration.tmdb.TmdbMovieContext;
import com.dumb.service.FileStorageService;
import com.dumb.service.MovieAiGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class MovieAiGenerationServiceImpl implements MovieAiGenerationService {

    private static final String SYSTEM_PROMPT = ""
        + "你是一位资深电影评论家，负责基于 TMDB 官方数据撰写影评。\n"
        + "你必须只输出一个 JSON 对象，不要输出任何其它文字、Markdown 或代码围栏。\n"
        + "JSON 字段：content（字符串）、genres（字符串数组，最多 5 个）、director（字符串）、actors（字符串）。\n"
        + "\n"
        + "【字段说明】\n"
        + "- content：电影评论正文，使用 HTML 片段。风格必须遵循以下要求：\n"
        + "  · 分段使用 emoji 小标题，如 <h3>🎬 剧情核心</h3>、<h3>🎭 表演与角色</h3>、<h3>🎨 视听风格</h3>、<h3>💭 总体评价</h3>\n"
        + "  · 每个段落用 <p>...</p> 包裹，短句为主，节奏轻快\n"
        + "  · 结尾必须有 <p><strong>📌 一句话推荐：</strong>...</p>\n"
        + "  · 正文为简体中文，400–800 个汉字\n"
        + "  · 内容须尊重事实，可引用 TMDB 数据作为参考但不要整段复制\n"
        + "  · 不要输出 <html> / <body>\n"
        + "- genres：从【中文风格候选列表】中选取最贴切的 1–5 个（必须完全匹配），参考 TMDB 的风格名称进行对应。\n"
        + "- director：导演姓名，如不确定则返回空字符串。\n"
        + "- actors：2–5 位主要主演，用逗号分隔；如不确定则返回空字符串。\n"
        + "\n"
        + "【中文风格候选列表】\n"
        + "剧情、喜剧、爱情、动作、冒险、奇幻、科幻、悬疑、惊悚、恐怖、\n"
        + "犯罪、黑帮、警匪、战争、历史、传记、音乐、歌舞、西部、武侠、\n"
        + "古装、宫廷、灾难、末日、丧尸、怪兽、怪谈、灵异、超自然、心理、\n"
        + "家庭、亲情、青春、校园、成长、体育、竞技、赛车、公路、美食、\n"
        + "职场、商战、政治、间谍、谍战、法庭、律政、医疗、军事、海战、\n"
        + "空战、太空、机甲、赛博朋克、蒸汽朋克、异世界、穿越、时间旅行、\n"
        + "平行世界、轮回、复仇、黑色电影、黑色幽默、讽刺、荒诞、实验电影、\n"
        + "艺术电影、独立电影、纪录片、纪录剧情片、动画、定格动画、黏土动画、\n"
        + "3D动画、短片、微电影、系列电影、史诗片、大片、B级片、Cult片、\n"
        + "公路片、歌剧电影、音乐纪录片。\n";

    private static final String FALLBACK_SYSTEM_PROMPT = ""
        + "你是一位资深电影评论家与资料库管理员，对全球电影有深入了解。\n"
        + "你必须只输出一个 JSON 对象，不要输出任何其它文字、Markdown 或代码围栏。\n"
        + "JSON 字段：title（字符串）、content（字符串）、score（字符串，0.0-10.0 之间的一位小数）、\n"
        + "director（字符串）、actors（字符串）、genres（字符串数组，最多 5 个）、\n"
        + "region（字符串）、releaseDate（字符串，YYYY-MM-DD 格式）、\n"
        + "coverImage（字符串，封面图 URL）、backgroundImage（字符串，背景图 URL）。\n"
        + "\n"
        + "【字段说明】\n"
        + "- title：电影的标准中文译名（或原名）。\n"
        + "- content：电影评论正文，使用 HTML 片段。风格要求同上。\n"
        + "- score：综合评分，0.0–10.0 之间，保留一位小数。\n"
        + "- director：导演姓名。\n"
        + "- actors：2–5 位主要主演，用逗号分隔。\n"
        + "- genres：从【中文风格候选列表】中选取最贴切的 1–5 个（必须完全匹配）。\n"
        + "- region：电影地区，可选值：中国大陆、中国香港、中国台湾、亚洲、美洲、欧洲、非洲。\n"
        + "- releaseDate：上映日期，格式 YYYY-MM-DD；若只有年份则填 YYYY-01-01。\n"
        + "- coverImage：尽量返回可靠封面图 URL；若不确定则返回空字符串。\n"
        + "- backgroundImage：尽量返回可靠背景图 URL；若不确定则返回空字符串。\n"
        + "\n"
        + "【中文风格候选列表】\n"
        + "剧情、喜剧、爱情、动作、冒险、奇幻、科幻、悬疑、惊悚、恐怖、\n"
        + "犯罪、黑帮、警匪、战争、历史、传记、音乐、歌舞、西部、武侠、\n"
        + "古装、宫廷、灾难、末日、丧尸、怪兽、怪谈、灵异、超自然、心理、\n"
        + "家庭、亲情、青春、校园、成长、体育、竞技、赛车、公路、美食、\n"
        + "职场、商战、政治、间谍、谍战、法庭、律政、医疗、军事、海战、\n"
        + "空战、太空、机甲、赛博朋克、蒸汽朋克、异世界、穿越、时间旅行、\n"
        + "平行世界、轮回、复仇、黑色电影、黑色幽默、讽刺、荒诞、实验电影、\n"
        + "艺术电影、独立电影、纪录片、纪录剧情片、动画、定格动画、黏土动画、\n"
        + "3D动画、短片、微电影、系列电影、史诗片、大片、B级片、Cult片、\n"
        + "公路片、歌剧电影、音乐纪录片。\n";

    private static final List<String> MOVIE_GENRE_OPTIONS = List.of(
        "剧情", "喜剧", "爱情", "动作", "冒险", "奇幻", "科幻", "悬疑", "惊悚", "恐怖",
        "犯罪", "黑帮", "警匪", "战争", "历史", "传记", "音乐", "歌舞", "西部", "武侠",
        "古装", "宫廷", "灾难", "末日", "丧尸", "怪兽", "怪谈", "灵异", "超自然", "心理",
        "家庭", "亲情", "青春", "校园", "成长", "体育", "竞技", "赛车", "公路", "美食",
        "职场", "商战", "政治", "间谍", "谍战", "法庭", "律政", "医疗", "军事", "海战",
        "空战", "太空", "机甲", "赛博朋克", "蒸汽朋克", "异世界", "穿越", "时间旅行", "平行世界", "轮回",
        "复仇", "黑色电影", "黑色幽默", "讽刺", "荒诞", "实验电影", "艺术电影", "独立电影", "纪录片", "纪录剧情片",
        "动画", "定格动画", "黏土动画", "3D动画", "短片", "微电影", "系列电影", "史诗片", "大片", "B级片",
        "Cult片", "公路片", "歌剧电影", "音乐纪录片"
    );

    private final TmdbClient tmdbClient;
    private final DeepSeekClient deepSeekClient;
    private final FileStorageService fileStorageService;

    public MovieAiGenerationServiceImpl(TmdbClient tmdbClient, DeepSeekClient deepSeekClient,
                                         FileStorageService fileStorageService) {
        this.tmdbClient = tmdbClient;
        this.deepSeekClient = deepSeekClient;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public MovieAiGenerateResponse generate(MovieAiGenerateRequest request) {
        String originalTitle = request.getTitle().trim();
        log.info("AI 电影生成请求: title={}", originalTitle);

        // 1. 先用原始名称查 TMDB
        Optional<TmdbMovieContext> movieOpt = tmdbClient.findMovie(originalTitle);
        if (movieOpt.isPresent()) {
            return generateWithTmdbData(originalTitle, movieOpt.get());
        }

        // 2. 未命中：降级到纯 AI
        log.warn("TMDB 未命中，降级到纯 AI 生成: title={}", originalTitle);
        return generateWithAiOnly(originalTitle);
    }

    private MovieAiGenerateResponse generateWithTmdbData(String displayTitle, TmdbMovieContext ctx) {
        String userPrompt = buildUserPrompt(ctx);
        AiMovieDraftJson draft = deepSeekClient.complete(SYSTEM_PROMPT, userPrompt, AiMovieDraftJson.class);

        MovieAiGenerateResponse response = new MovieAiGenerateResponse();
        response.setTitle(displayTitle);
        response.setContent(trimToNull(draft.getContent()));
        response.setDirector(trimToNull(draft.getDirector()));
        response.setActors(trimToNull(draft.getActors()));
        response.setGenres(filterAndMapGenres(draft.getGenres()));
        response.setScore(ctx.getVoteAverage() != null ? ctx.getVoteAverage() : parseScore(draft.getScore()));
        response.setReleaseDate(ctx.getReleaseDate());
        response.setRegion(mapRegion(ctx.getProductionCountries()));

        // 下载封面和背景图
        String coverLocal = downloadImageSafely(tmdbClient.buildImageUrl(ctx.getPosterPath()), displayTitle, "poster");
        response.setCoverImage(coverLocal != null ? coverLocal : tmdbClient.buildImageUrl(ctx.getPosterPath()));

        String bgLocal = downloadImageSafely(tmdbClient.buildImageUrl(ctx.getBackdropPath()), displayTitle, "backdrop");
        response.setBackgroundImage(bgLocal != null ? bgLocal : tmdbClient.buildImageUrl(ctx.getBackdropPath()));

        if (!StringUtils.hasText(response.getContent())) {
            response.setContent("<p>（AI 未生成正文，请手动撰写电影简介。）</p>");
        }
        return response;
    }

    private MovieAiGenerateResponse generateWithAiOnly(String title) {
        String userPrompt = String.format("请为电影《%s》生成完整的资料与评论 JSON。", title);
        AiMovieDraftJson draft = deepSeekClient.complete(FALLBACK_SYSTEM_PROMPT, userPrompt, AiMovieDraftJson.class);

        MovieAiGenerateResponse response = new MovieAiGenerateResponse();
        response.setTitle(StringUtils.hasText(draft.getTitle()) ? draft.getTitle().trim() : title);
        response.setContent(trimToNull(draft.getContent()));
        response.setScore(parseScore(draft.getScore()));
        response.setDirector(trimToNull(draft.getDirector()));
        response.setActors(trimToNull(draft.getActors()));
        response.setGenres(filterAndMapGenres(draft.getGenres()));
        response.setRegion(trimToNull(draft.getRegion()));
        response.setReleaseDate(trimToNull(draft.getReleaseDate()));
        response.setCoverImage(trimToNull(draft.getCoverImage()));
        response.setBackgroundImage(trimToNull(draft.getBackgroundImage()));

        if (!StringUtils.hasText(response.getContent())) {
            response.setContent("<p>（AI 未生成正文，请手动撰写电影简介。）</p>");
        }
        return response;
    }

    private static String buildUserPrompt(TmdbMovieContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("【TMDB 官方数据（作为写作事实依据）】\n");
        sb.append("标题：").append(ctx.getTitle()).append("\n");
        if (ctx.getVoteAverage() != null) {
            sb.append("评分：").append(ctx.getVoteAverage()).append("/10\n");
        }
        if (ctx.getGenres() != null && !ctx.getGenres().isEmpty()) {
            sb.append("风格标签（TMDB）：").append(String.join(", ", ctx.getGenres())).append("\n");
        }
        if (StringUtils.hasText(ctx.getReleaseDate())) {
            sb.append("上映日期：").append(ctx.getReleaseDate()).append("\n");
        }
        if (StringUtils.hasText(ctx.getOverview())) {
            String desc = ctx.getOverview()
                .replaceAll("<[^>]*>", "")
                .replace("\n", " ")
                .trim();
            if (desc.length() > 300) {
                desc = desc.substring(0, 300) + "...";
            }
            sb.append("官方简介摘要：").append(desc).append("\n");
        }
        sb.append("\n要求：content 为 HTML 影评正文；genres 从【中文风格候选列表】中选取最匹配的 1–5 个；director 和 actors 尽量准确。");
        return sb.toString();
    }

    private static BigDecimal parseScore(String raw) {
        if (!StringUtils.hasText(raw)) {
            return BigDecimal.valueOf(8.0);
        }
        try {
            BigDecimal score = new BigDecimal(raw.trim());
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            }
            if (score.compareTo(BigDecimal.TEN) > 0) {
                score = BigDecimal.TEN;
            }
            return score.setScale(1, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.valueOf(8.0);
        }
    }

    private static List<String> filterAndMapGenres(List<String> draftGenres) {
        if (draftGenres == null || draftGenres.isEmpty()) {
            return List.of();
        }
        return draftGenres.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .filter(MOVIE_GENRE_OPTIONS::contains)
            .distinct()
            .limit(5)
            .toList();
    }

    private String downloadImageSafely(String imageUrl, String title, String type) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        try {
            String localPath = fileStorageService.downloadRemoteImage(imageUrl);
            log.info("电影图片已落盘: title={}, type={}, localPath={}", title, type, localPath);
            return localPath;
        } catch (Exception e) {
            log.warn("电影图片下载失败，已跳过: title={}, type={}, url={}, reason={}",
                title, type, imageUrl, e.getMessage());
            return null;
        }
    }

    private static String mapRegion(List<String> isoCountries) {
        if (isoCountries == null || isoCountries.isEmpty()) {
            return null;
        }
        // 取第一个国家码映射
        String iso = isoCountries.get(0).toUpperCase();
        return switch (iso) {
            case "CN" -> "中国大陆";
            case "HK" -> "中国香港";
            case "TW" -> "中国台湾";
            case "JP", "KR", "IN", "TH", "VN", "ID", "MY", "PH", "SG" -> "亚洲";
            case "US", "CA", "MX", "BR", "AR", "CL", "CO", "PE", "VE", "CU", "JM", "DO", "PR" -> "美洲";
            case "GB", "FR", "DE", "IT", "ES", "PT", "NL", "BE", "CH", "AT", "SE", "NO", "DK", "FI", "PL", "CZ", "HU", "RO", "BG", "HR", "RS", "GR", "RU", "UA", "BY" -> "欧洲";
            case "EG", "ZA", "NG", "KE", "MA", "ET", "GH", "TZ", "UG", "DZ", "TN" -> "非洲";
            default -> null;
        };
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
