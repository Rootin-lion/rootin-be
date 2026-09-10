package com.example.rootin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400, "C400", "Bad request."),
    UNAUTHORIZED(401, "C401", "Authentication is required."),
    FORBIDDEN(403, "C403", "Access is forbidden."),
    NOT_FOUND(404, "C404", "Resource not found."),
    CONFLICT(409, "C409", "Conflict."),
    OAUTH_PROVIDER_ERROR(502, "C502", "OAuth provider request failed."),
    INVALID_JWT_SECRET(500, "C503", "JWT secret is misconfigured."),
    INTERNAL_SERVER_ERROR(500, "C500", "Internal server error."),

    COMPETITION_NOT_FOUND(404, "C404-1", "Competition not found."),
    COMPETITION_NOT_JOINABLE(400, "C400-1", "Competition is not joinable."),
    COMPETITION_ALREADY_JOINED(409, "C409-1", "Competition already joined."),

    GEMINI_BAD_REQUEST(400, "C504", "Gemini request failed."),
    GEMINI_RATE_LIMITED(429, "C429", "Gemini rate limit exceeded."),
    GEMINI_SERVER_ERROR(502, "C505", "Gemini server error."),
    GEMINI_TIMEOUT(504, "C506", "Gemini request timed out."),
    GEMINI_JSON_PARSE_ERROR(500, "C507", "Gemini response parsing failed.");

    private final int status;
    private final String code;
    private final String message;
}
