package com.dumb.service.impl;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.ReviewAiGenerateRequest;
import com.dumb.dto.response.ReviewAiGenerateResponse;
import com.dumb.integration.ai.AiDraftJson;
import com.dumb.integration.ai.DeepSeekClient;
import com.dumb.integration.lastfm.LastFmAlbumContext;
import com.dumb.integration.lastfm.LastFmClient;
import lombok.extern.slf4j.Slf4j;
import com.dumb.service.FileStorageService;
import com.dumb.service.ReviewAiGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ReviewAiGenerationServiceImpl implements ReviewAiGenerationService {

    private static final String SYSTEM_PROMPT = ""
        + "你是一位熟悉音乐场景的专业乐评人，尤其了解全球的音乐生态。\n"
        + "你必须只输出一个 JSON 对象，不要输出任何其它文字、Markdown 或代码围栏。\n"
        + "JSON 字段：year（字符串）、tracks（字符串）、leadTrack（字符串）、description（字符串）。\n"
        + "\n"
        + "【唯一模式：已从 Last.fm 锁定完整官方曲目】\n"
        + "- tracks：必须为空字符串 \"\"（服务端会用 Last.fm 列表拼接，禁止你填写曲目）。\n"
        + "- leadTrack：任选一首主打，填入与官方列表中某一曲名完全一致的字符串（含标点、大小写、feat. 等）；不确定则填 \"\"。\n"
        + "- description：乐评正文，可引用用户消息中的官方曲名进行分析，但不要输出完整曲目表。\n"
        + "- year：必须与用户给定年份完全一致。\n"
        + "\n"
        + "description 使用 HTML 片段：多个 <p>...</p>，可适度 <strong>，不要 <html>/<body>。\n"
        + "正文为简体中文，600–1000 个汉字（近似），须满足：\n"
        + "从「音乐场景」切入但不完全被场景定义；文艺克制、有画面感；结合具体曲目写旋律、编曲、情绪与听感；\n"
        + "可类比其它艺人/厂牌/阶段；强调年轻气质、乐迷属性、场景归属但要有反思；分析风格融合；\n"
        + "结尾整体评价并上升到当下音乐环境；句式偏长有节奏，理性与感性兼顾，适当批判。\n"
        + "内容须尊重事实、避免人身攻击与侵权表述。\n";

    private final LastFmClient lastFmClient;
    private final DeepSeekClient deepSeekClient;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public ReviewAiGenerationServiceImpl(
        LastFmClient lastFmClient,
        DeepSeekClient deepSeekClient,
        FileStorageService fileStorageService,
        ObjectMapper objectMapper
    ) {
        this.lastFmClient = lastFmClient;
        this.deepSeekClient = deepSeekClient;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReviewAiGenerateResponse generate(ReviewAiGenerateRequest request) {
        String artist = request.getArtist().trim();
        String albumTitle = request.getAlbumTitle().trim();
        String inputYear = request.getYear().trim();
        log.info("AI 乐评生成请求: artist={}, albumTitle={}, year={}", artist, albumTitle, inputYear);

        Optional<LastFmAlbumContext> albumOpt = lastFmClient.findAlbum(artist, albumTitle);
        LastFmAlbumContext ctx = albumOpt.orElseThrow(() ->
            new BizException(ResultCodeEnum.PARAM_ERROR, "未从 Last.fm 匹配到专辑，请检查艺人/专辑名称或补充 LASTFM_API_KEY")
        );
        log.info("Last.fm 命中专辑: matchedArtist={}, matchedAlbum={}, trackCount={}",
            ctx.getArtistName(), ctx.getAlbumName(), ctx.getTrackNames() == null ? 0 : ctx.getTrackNames().size());
        if (ctx.getTrackNames() == null || ctx.getTrackNames().isEmpty()) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "已匹配到 Last.fm 专辑，但未拿到官方曲目列表，请更换艺人/专辑名称后重试");
        }
        String sourceJson = albumContextToJson(ctx);
        int n = ctx.getTrackNames().size();
        String userPrompt = String.format(
            ""
                + "【已从 Last.fm 锁定完整官方曲目，共 %d 首，请仅按官方曲目写作】\n"
                + "艺人：%s\n"
                + "专辑：%s\n\n"
                + "用户指定年份（必须严格使用此值）：%s\n\n"
                + "专辑元数据（含按播放顺序的完整曲目，写作时可引用曲名）：%s\n\n"
                + "要求：tracks 填 \"\"；leadTrack 填与上述曲目中某一项完全一致的主打曲名，或 \"\"；\n"
                + "year 必须等于用户指定年份；description 为 HTML 乐评正文，勿罗列完整曲目表。\n",
            n,
            artist,
            albumTitle,
            inputYear,
            sourceJson
        );

        AiDraftJson draft = deepSeekClient.completeDraft(SYSTEM_PROMPT, userPrompt);

        ReviewAiGenerateResponse response = new ReviewAiGenerateResponse();
        response.setYear(inputYear);
        response.setDescription(trimToNull(draft.getDescription()));
        response.setTracks(buildTracksFromSource(ctx.getTrackNames(), trimToNull(draft.getLeadTrack())));

        if (!StringUtils.hasText(response.getDescription())) {
            response.setDescription("<p>（AI 未生成正文，请手动撰写乐评。）</p>");
        }
        String coverImage = downloadCoverSafely(ctx.getCoverImageUrl(), artist, albumTitle);
        if (StringUtils.hasText(coverImage)) {
            response.setCoverImage(coverImage);
        } else if (StringUtils.hasText(ctx.getCoverImageUrl())) {
            // 下载失败时直接使用 Last.fm 原始图片 URL
            response.setCoverImage(ctx.getCoverImageUrl());
        }

        response.setRawAlbumMeta(buildRawMeta(Optional.of(ctx)));
        return response;
    }

    /**
     * 曲目列表 100% 使用数据源返回顺序；仅在匹配的曲名后加一颗 ⭐。
     */
    private static String buildTracksFromSource(List<String> names, String leadTrack) {
        String lead = StringUtils.hasText(leadTrack) ? leadTrack.trim() : null;
        boolean starred = false;
        List<String> parts = new ArrayList<>();
        for (String raw : names) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            boolean mark = false;
            if (lead != null && !starred) {
                if (raw.equals(lead) || raw.trim().equals(lead)) {
                    mark = true;
                    starred = true;
                }
            }
            parts.add(mark ? raw + "⭐" : raw);
        }
        return String.join(" / ", parts);
    }

    private static Map<String, Object> buildRawMeta(Optional<LastFmAlbumContext> albumOpt) {
        if (albumOpt.isEmpty()) {
            return null;
        }
        LastFmAlbumContext ctx = albumOpt.get();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("source", "lastfm");
        meta.put("lastfmUrl", ctx.getUrl());
        meta.put("lastfmMbid", ctx.getMbid());
        meta.put("matchedAlbumName", ctx.getAlbumName());
        meta.put("matchedArtistName", ctx.getArtistName());
        meta.put("releaseDate", ctx.getReleaseDate());
        meta.put("sourceCoverImageUrl", ctx.getCoverImageUrl());
        List<String> names = ctx.getTrackNames();
        meta.put("trackCount", names != null ? names.size() : 0);
        return meta;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String albumContextToJson(LastFmAlbumContext ctx) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("albumName", ctx.getAlbumName());
            m.put("artistName", ctx.getArtistName());
            m.put("releaseDate", ctx.getReleaseDate());
            m.put("releaseYear", ctx.getReleaseYear());
            m.put("tracks", ctx.getTrackNames());
            m.put("url", ctx.getUrl());
            m.put("mbid", ctx.getMbid());
            m.put("coverImageUrl", ctx.getCoverImageUrl());
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String downloadCoverSafely(String coverUrl, String artist, String albumTitle) {
        if (!StringUtils.hasText(coverUrl)) {
            return null;
        }
        try {
            String localPath = fileStorageService.downloadRemoteImage(coverUrl);
            log.info("专辑封面已落盘: artist={}, albumTitle={}, localPath={}", artist, albumTitle, localPath);
            return localPath;
        } catch (Exception e) {
            log.warn("专辑封面下载失败，已跳过: artist={}, albumTitle={}, coverUrl={}, reason={}",
                artist, albumTitle, coverUrl, e.getMessage());
            return null;
        }
    }
}
