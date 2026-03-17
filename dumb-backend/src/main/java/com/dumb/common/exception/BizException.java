package com.dumb.common.exception;

import com.dumb.common.enums.ResultCodeEnum;
import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final ResultCodeEnum code;

    public BizException(ResultCodeEnum code, String message) {
        super(message);
        this.code = code;
    }
}
