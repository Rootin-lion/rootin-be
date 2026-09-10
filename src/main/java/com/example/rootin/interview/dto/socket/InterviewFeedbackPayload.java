package com.example.rootin.interview.dto.socket;

import java.util.List;

public record InterviewFeedbackPayload(
        Long questionId,
        Long answerId,
        String answerText,
        String feedback,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        Double keywordCoverage,
        boolean topicCompleted
) {
}
