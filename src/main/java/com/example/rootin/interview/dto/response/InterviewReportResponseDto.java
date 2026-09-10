package com.example.rootin.interview.dto.response;

import java.util.List;

public record InterviewReportResponseDto(
        Long interviewId,
        Double overallKeywordCoverage,
        List<InterviewTopicReportDto> topicReports,
        List<String> learningGuide,
        String summary,
        Double averageScore,
        List<InterviewQuestionReportDto> questionReports
) {
}
