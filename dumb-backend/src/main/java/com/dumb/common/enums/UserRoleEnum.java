package com.dumb.common.enums;

import com.dumb.common.enums.base.BaseEnum;
import lombok.Getter;

@Getter
public enum UserRoleEnum implements BaseEnum<String> {
    ADMIN("ADMIN", "管理员"),
    EDITOR("EDITOR", "编辑"),
    USER("USER", "普通用户");

    private final String code;
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
