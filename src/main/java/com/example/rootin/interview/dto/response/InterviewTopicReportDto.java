package com.example.rootin.interview.dto.response;

import java.util.List;

public record InterviewTopicReportDto(
        Long topicId,
        String topicName,
        int topicOrder,
        Double keywordCoverage,
        List<String> matchedKeywords,
        List<String> missingKeywords
//        int basicQuestionCount,
//        int followUpQuestionCount
) {
}
