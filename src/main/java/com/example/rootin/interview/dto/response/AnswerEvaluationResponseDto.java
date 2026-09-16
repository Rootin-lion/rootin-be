package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.AnswerLevel;

public record AnswerEvaluationResponseDto( //AI 응답 평가
        AnswerLevel level,
        int accuracy,
        String feedback,
        String matchedKeywords,
        String missingKeywords
) {
}
