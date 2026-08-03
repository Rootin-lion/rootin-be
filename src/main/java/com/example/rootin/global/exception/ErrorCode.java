package com.example.rootin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400, "C400", "잘못된 요청입니다."),
    UNAUTHORIZED(401, "C401", "인증이 필요합니다."),
    FORBIDDEN(403, "C403", "접근할 수 없습니다."),
    NOT_FOUND(404, "C404", "정보를 찾을 수 없습니다."),
    COMPETITION_NOT_FOUND(404, "C404-1", "해당 날짜에 진행되는 대회가 없습니다."),
    CONFLICT(409, "C409", "요청이 현재 상태와 충돌합니다."),
    OAUTH_PROVIDER_ERROR(502, "C502", "외부 인증 서비스 처리에 실패했습니다."),
    INVALID_JWT_SECRET(500, "C503", "JWT Secret 설정이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(500, "C500", "서버 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
