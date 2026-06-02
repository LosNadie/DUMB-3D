package com.dumb.integration.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * DeepSeek 返回的 JSON 对象结构（与提示词约定一致）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiDraftJson {

    private String year;

    private String tracks;

    /**
     * 当有官方曲目列表时由模型指定主打曲名（须与列表中某一曲名完全一致），服务端用于加 ⭐；无官方列表时可不填。
     */
    private String leadTrack;

    private String description;

    private String coverImage;
}
