package com.dumb.common.enums;

import com.dumb.common.enums.base.BaseEnum;
import lombok.Getter;

@Getter
public enum ContentTypeEnum implements BaseEnum<String> {
    REVIEW("REVIEW", "乐评"),
    NEWS("NEWS", "新闻"),
    INTERVIEW("INTERVIEW", "访谈");

    private final String code;
    private final String desc;

    ContentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
