package com.example.rootin.interview.dto.response;


import com.example.rootin.interview.domain.InterviewMode;
import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewReport;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDateTime;
import java.util.List;

public record MypageReportResponseDto(
        Long reportId,
        Long interviewId,
        InterestField category,
        String title,
        InterviewMode mode,
        int questionCount,
        LocalDateTime completedAt,
        Integer averageAccuracy,
        String overallFeedback
) {
    public static MypageReportResponseDto from(InterviewReport report, List<String> topicNames) {
        Interview interview = report.getInterviewId();

        return new MypageReportResponseDto(
                report.getId(),
                interview.getId(),
                interview.getCategory(),
                createTitle(topicNames),
                InterviewMode.TEXT,
                interview.getQuestionCount(),
                interview.getCompletedAt(),
                report.getAverageAccuracy(),
                report.getOverallFeedback()
        );
    }
    // ex) 데드락, 동기화 문제 외 1개 면접
    private static String createTitle(List<String> topicNames) {
        if (topicNames.isEmpty()) {return "기술 면접";}

        if (topicNames.size() == 1) {return topicNames.get(0) + " 면접";}

        String firstTwoTopics = String.join(", ", topicNames.subList(0, 2));
        if (topicNames.size() == 2) {return firstTwoTopics + " 면접";}

        return firstTwoTopics + " 외 " + (topicNames.size() - 2) + "개 면접";
    }
}

