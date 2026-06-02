package com.dumb.service.impl;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.AnimeAiGenerateRequest;
import com.dumb.dto.response.AnimeAiGenerateResponse;
import com.dumb.integration.ai.AiAnimeDraftJson;
import com.dumb.integration.ai.DeepSeekClient;
import com.dumb.integration.anilist.AniListAnimeContext;
import com.dumb.integration.anilist.AniListClient;
import com.dumb.service.AnimeAiGenerationService;
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
public class AnimeAiGenerationServiceImpl implements AnimeAiGenerationService {

    private static final String SYSTEM_PROMPT = ""
        + "你是一位资深动漫评论家，负责基于 AniList 官方数据撰写评论。\n"
        + "你必须只输出一个 JSON 对象，不要输出任何其它文字、Markdown 或代码围栏。\n"
        + "JSON 字段：content（字符串）、genres（字符串数组，最多 5 个）。\n"
        + "\n"
        + "【字段说明】\n"
        + "- content：动漫评论正文，使用 HTML 片段。风格必须遵循以下要求：\n"
        + "  · 分段使用 emoji 小标题，如 <h3>🎩 人物设定</h3>、<h3>🌀 世界观 / 剧情核心</h3>、<h3>🌸 氛围质感</h3>、<h3>💫 风格特色</h3>、<h3>✨ 为什么值得看</h3>\n"
        + "  · 每个段落用 <p>...</p> 包裹，短句为主，节奏轻快，可适当使用颜文字 (´･ω･`) 或 emoji\n"
        + "  · 结尾必须有 <p><strong>📌 一句话推荐：</strong>...</p>\n"
        + "  · 正文为简体中文，400–800 个汉字\n"
        + "  · 内容须尊重事实，可引用 AniList 数据作为参考但不要整段复制\n"
        + "  · 不要输出 <html> / <body>\n"
        + "- genres：从【中文风格候选列表】中选取最贴切的 1–5 个（必须完全匹配），参考 AniList 的英文 genres 进行对应。\n"
        + "\n"
        + "【中文风格候选列表】\n"
        + "热血、战斗、格斗、冒险、王道、少年、少女、青年、后宫、逆后宫、恋爱、纯爱、青春、校园、治愈、"
        + "催泪、悬疑、推理、侦探、犯罪、心理、智斗、黑暗、致郁、科幻、未来、赛博朋克、机甲、太空、"
        + "异世界、穿越、转生、奇幻、魔法、超能力、妖怪、鬼怪、神话、武侠、历史、战争、军事、竞技、"
        + "运动、音乐、偶像、歌舞、搞笑、日常、美食、职场、社会、政治、家庭、亲情、友情、成长、生存、"
        + "末日、丧尸、灾难、时间循环、平行世界、轮回、重生、系统流、升级流、种田流、爽文、轻小说改、"
        + "漫画改、原创、剧场版、OVA、泡面番、百合、BL、GL、兄妹、青梅竹马、傲娇、病娇、天然呆、腹黑、"
        + "反派、反英雄、暗黑英雄、悬疑反转、群像、单元剧、公路片、蒸汽波、赛博、国风、古风、玄幻、"
        + "修真、神作、冷门、神展开、开放结局。\n";

    private static final List<String> ANIME_GENRE_OPTIONS = List.of(
        "热血", "战斗", "格斗", "冒险", "王道", "少年", "少女", "青年", "后宫", "逆后宫",
        "恋爱", "纯爱", "青春", "校园", "治愈", "催泪", "悬疑", "推理", "侦探", "犯罪",
        "心理", "智斗", "黑暗", "致郁", "科幻", "未来", "赛博朋克", "机甲", "太空", "异世界",
        "穿越", "转生", "奇幻", "魔法", "超能力", "妖怪", "鬼怪", "神话", "武侠", "历史",
        "战争", "军事", "竞技", "运动", "音乐", "偶像", "歌舞", "搞笑", "日常", "美食",
        "职场", "社会", "政治", "家庭", "亲情", "友情", "成长", "生存", "末日", "丧尸",
        "灾难", "时间循环", "平行世界", "轮回", "重生", "系统流", "升级流", "种田流", "爽文", "轻小说改",
        "漫画改", "原创", "剧场版", "OVA", "泡面番", "百合", "BL", "GL", "兄妹", "青梅竹马",
        "傲娇", "病娇", "天然呆", "腹黑", "反派", "反英雄", "暗黑英雄", "悬疑反转", "群像", "单元剧",
        "公路片", "蒸汽波", "赛博", "国风", "古风", "玄幻", "修真", "神作", "冷门", "神展开", "开放结局"
    );

    private final AniListClient aniListClient;
    private final DeepSeekClient deepSeekClient;

    public AnimeAiGenerationServiceImpl(AniListClient aniListClient, DeepSeekClient deepSeekClient) {
        this.aniListClient = aniListClient;
        this.deepSeekClient = deepSeekClient;
    }

    private static final String FALLBACK_SYSTEM_PROMPT = ""
        + "你是一位资深动漫评论家与资料库管理员，对全球动漫作品有深入了解。\n"
        + "你必须只输出一个 JSON 对象，不要输出任何其它文字、Markdown 或代码围栏。\n"
        + "JSON 字段：title（字符串）、content（字符串）、score（字符串，0.0-10.0 之间的一位小数）、"
        + "studio（字符串）、genres（字符串数组，最多 5 个）、releaseDate（字符串，YYYY-MM-DD 格式）、"
        + "coverImage（字符串，封面图 URL）、backgroundImage（字符串，背景图 URL）。\n"
        + "\n"
        + "【字段说明】\n"
        + "- title：动漫的标准中文译名（或原名）。\n"
        + "- content：动漫评论正文，使用 HTML 片段。风格必须遵循以下要求：\n"
        + "  · 分段使用 emoji 小标题，如 <h3>🎩 人物设定</h3>、<h3>🌀 世界观 / 剧情核心</h3>、<h3>🌸 氛围质感</h3>、<h3>💫 风格特色</h3>、<h3>✨ 为什么值得看</h3>\n"
        + "  · 每个段落用 <p>...</p> 包裹，短句为主，节奏轻快，可适当使用颜文字 (´･ω･`) 或 emoji\n"
        + "  · 结尾必须有 <p><strong>📌 一句话推荐：</strong>...</p>\n"
        + "  · 正文为简体中文，400–800 个汉字\n"
        + "  · 内容须尊重事实\n"
        + "  · 不要输出 <html> / <body>\n"
        + "- score：综合评分，0.0–10.0 之间，保留一位小数。\n"
        + "- studio：主要动画制作公司。\n"
        + "- genres：从【中文风格候选列表】中选取最贴切的 1–5 个（必须完全匹配）。\n"
        + "- releaseDate：首播日期，格式 YYYY-MM-DD；若只有年份则填 YYYY-01-01。\n"
        + "- coverImage：尽量返回可靠封面图 URL；若不确定则返回空字符串。\n"
        + "- backgroundImage：尽量返回可靠背景图 URL；若不确定则返回空字符串。\n"
        + "\n"
        + "【中文风格候选列表】\n"
        + "热血、战斗、格斗、冒险、王道、少年、少女、青年、后宫、逆后宫、恋爱、纯爱、青春、校园、治愈、"
        + "催泪、悬疑、推理、侦探、犯罪、心理、智斗、黑暗、致郁、科幻、未来、赛博朋克、机甲、太空、"
        + "异世界、穿越、转生、奇幻、魔法、超能力、妖怪、鬼怪、神话、武侠、历史、战争、军事、竞技、"
        + "运动、音乐、偶像、歌舞、搞笑、日常、美食、职场、社会、政治、家庭、亲情、友情、成长、生存、"
        + "末日、丧尸、灾难、时间循环、平行世界、轮回、重生、系统流、升级流、种田流、爽文、轻小说改、"
        + "漫画改、原创、剧场版、OVA、泡面番、百合、BL、GL、兄妹、青梅竹马、傲娇、病娇、天然呆、腹黑、"
        + "反派、反英雄、暗黑英雄、悬疑反转、群像、单元剧、公路片、蒸汽波、赛博、国风、古风、玄幻、"
        + "修真、神作、冷门、神展开、开放结局。\n";

    private static final String TRANSLATE_SYSTEM_PROMPT = ""
        + "你是一位精通中日英三语的动漫资料专家。\n"
        + "请将用户提供的中文动漫名称翻译成它在 AniList 数据库中最可能匹配的官方名称。\n"
        + "优先返回日文原名（如：古見さんは、コミュ症です。），其次返回英文官方名（如：Komi Can't Communicate）。\n"
        + "只输出一个 JSON 对象：{\"title\": \"翻译后的名称\"}。不要输出任何其他文字、解释或 Markdown。\n";

    @Override
    public AnimeAiGenerateResponse generate(AnimeAiGenerateRequest request) {
        String originalTitle = request.getTitle().trim();
        log.info("AI 动漫生成请求: title={}", originalTitle);

        // 1. 先用原始名称查 AniList
        Optional<AniListAnimeContext> animeOpt = aniListClient.findAnime(originalTitle);
        if (animeOpt.isPresent()) {
            return generateWithAniListData(originalTitle, animeOpt.get());
        }

        // 2. 未命中：用 DeepSeek 把中文翻译成日文/英文名
        String translatedTitle = translateTitle(originalTitle);
        if (StringUtils.hasText(translatedTitle) && !translatedTitle.equals(originalTitle)) {
            log.info("尝试用翻译后的名称查询 AniList: original={}, translated={}", originalTitle, translatedTitle);
            animeOpt = aniListClient.findAnime(translatedTitle);
            if (animeOpt.isPresent()) {
                return generateWithAniListData(originalTitle, animeOpt.get());
            }
        }

        // 3. 还是没命中：降级到纯 AI
        log.warn("AniList 未命中，降级到纯 AI 生成: title={}", originalTitle);
        return generateWithAiOnly(originalTitle);
    }

    private String translateTitle(String title) {
        try {
            String userPrompt = String.format("动漫名称：《%s》", title);
            Map<String, String> result = deepSeekClient.complete(
                TRANSLATE_SYSTEM_PROMPT, userPrompt, Map.class
            );
            String translated = result != null ? result.get("title") : null;
            if (StringUtils.hasText(translated)) {
                translated = translated.trim();
                // 去掉可能的引号
                translated = translated.replaceAll("^\"+|\"+$", "");
                return translated;
            }
        } catch (Exception e) {
            log.warn("DeepSeek 名称翻译失败: title={}, reason={}", title, e.getMessage());
        }
        return null;
    }

    private AnimeAiGenerateResponse generateWithAniListData(String displayTitle, AniListAnimeContext ctx) {
        String userPrompt = buildUserPrompt(ctx);
        AiAnimeDraftJson draft = deepSeekClient.complete(SYSTEM_PROMPT, userPrompt, AiAnimeDraftJson.class);

        AnimeAiGenerateResponse response = new AnimeAiGenerateResponse();
        response.setTitle(displayTitle);
        response.setContent(trimToNull(draft.getContent()));
        response.setScore(aniListScoreToDecimal(ctx.getAverageScore()));
        response.setStudio(ctx.getStudio());
        response.setGenres(filterAndMapGenres(draft.getGenres()));
        response.setReleaseDate(ctx.getReleaseDate());
        response.setCoverImage(ctx.getCoverImageUrl());
        response.setBackgroundImage(ctx.getBannerImageUrl());

        if (!StringUtils.hasText(response.getContent())) {
            response.setContent("<p>（AI 未生成正文，请手动撰写动漫评论。）</p>");
        }
        return response;
    }

    private AnimeAiGenerateResponse generateWithAiOnly(String title) {
        String userPrompt = String.format("请为动漫《%s》生成完整的资料与评论 JSON。", title);
        AiAnimeDraftJson draft = deepSeekClient.complete(FALLBACK_SYSTEM_PROMPT, userPrompt, AiAnimeDraftJson.class);

        AnimeAiGenerateResponse response = new AnimeAiGenerateResponse();
        response.setTitle(StringUtils.hasText(draft.getTitle()) ? draft.getTitle().trim() : title);
        response.setContent(trimToNull(draft.getContent()));
        response.setScore(parseScore(draft.getScore()));
        response.setStudio(trimToNull(draft.getStudio()));
        response.setGenres(filterAndMapGenres(draft.getGenres()));
        response.setReleaseDate(trimToNull(draft.getReleaseDate()));
        response.setCoverImage(trimToNull(draft.getCoverImage()));
        response.setBackgroundImage(trimToNull(draft.getBackgroundImage()));

        if (!StringUtils.hasText(response.getContent())) {
            response.setContent("<p>（AI 未生成正文，请手动撰写动漫评论。）</p>");
        }
        return response;
    }

    private static String buildUserPrompt(AniListAnimeContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AniList 官方数据（作为写作事实依据）】\n");
        sb.append("标题：").append(ctx.getTitle()).append("\n");
        if (StringUtils.hasText(ctx.getStudio())) {
            sb.append("制作公司：").append(ctx.getStudio()).append("\n");
        }
        if (ctx.getGenres() != null && !ctx.getGenres().isEmpty()) {
            sb.append("风格标签（AniList）：").append(String.join(", ", ctx.getGenres())).append("\n");
        }
        if (StringUtils.hasText(ctx.getReleaseDate())) {
            sb.append("首播日期：").append(ctx.getReleaseDate()).append("\n");
        }
        if (ctx.getAverageScore() != null) {
            sb.append("AniList 评分：").append(ctx.getAverageScore()).append("/100\n");
        }
        if (StringUtils.hasText(ctx.getDescription())) {
            String desc = ctx.getDescription()
                .replaceAll("<[^>]*>", "")
                .replace("\n", " ")
                .trim();
            if (desc.length() > 300) {
                desc = desc.substring(0, 300) + "...";
            }
            sb.append("官方简介摘要：").append(desc).append("\n");
        }
        sb.append("\n要求：content 为 HTML 评论正文；genres 从【中文风格候选列表】中选取最匹配的 1–5 个。");
        return sb.toString();
    }

    private static BigDecimal aniListScoreToDecimal(Integer aniListScore) {
        if (aniListScore == null) {
            return BigDecimal.valueOf(8.0);
        }
        return BigDecimal.valueOf(aniListScore)
            .divide(BigDecimal.valueOf(10), 1, RoundingMode.HALF_UP);
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
            .filter(ANIME_GENRE_OPTIONS::contains)
            .distinct()
            .limit(5)
            .toList();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
