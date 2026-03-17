package com.dumb.common.enums;

import com.dumb.common.enums.base.BaseEnum;
import lombok.Getter;

@Getter
public enum ContentStatusEnum implements BaseEnum<String> {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String desc;

    ContentStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
