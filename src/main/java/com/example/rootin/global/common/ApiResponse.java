package com.example.rootin.global.common;

import com.example.rootin.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final boolean success;
    private final String errorCode;
    private final String message;
    private final Object detail;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, "정상적으로 처리되었습니다.", null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, null, message, null, data);
    }

    public static ApiResponse<Object> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    public static ApiResponse<Object> error(ErrorCode errorCode, Object detail) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), detail, null);
    }
}
