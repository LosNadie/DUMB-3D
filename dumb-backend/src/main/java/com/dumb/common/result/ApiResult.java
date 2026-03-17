package com.dumb.common.result;

import com.dumb.common.enums.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResult<T> success() {
        return success(null);
    }

    public static <T> ApiResult<T> fail(ResultCodeEnum resultCodeEnum) {
        return new ApiResult<>(resultCodeEnum.getCode(), resultCodeEnum.getMessage(), null);
    }

    public static <T> ApiResult<T> fail(ResultCodeEnum resultCodeEnum, String message) {
        return new ApiResult<>(resultCodeEnum.getCode(), message, null);
    }
}
