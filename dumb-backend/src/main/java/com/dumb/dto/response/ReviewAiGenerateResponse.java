package com.dumb.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class ReviewAiGenerateResponse {

    /**
     * 发行年份，4 位数字字符串
     */
    private String year;

    /**
     * 曲目列表，与前台约定一致：使用 " / " 分隔
     */
    private String tracks;

    /**
     * 乐评正文（HTML 片段，供富文本编辑器使用）
     */
    private String description;

    /**
     * 自动拉取并落盘后的封面地址（站内路径，如 /uploads/xxx.jpg）
     */
    private String coverImage;

    /**
     * Last.fm 等元数据摘要，便于排查匹配结果（可为空）
     */
    private Map<String, Object> rawAlbumMeta;
}
