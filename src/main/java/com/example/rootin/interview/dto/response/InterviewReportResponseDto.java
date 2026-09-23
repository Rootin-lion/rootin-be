package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewEvaluation;
import com.example.rootin.interview.domain.InterviewReport;
import com.example.rootin.interview.domain.InterviewReportStatus;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDateTime;
import java.util.List;

//최종리포트 GET
public record InterviewReportResponseDto(
        Long interviewId,
        InterestField category,
        int questionCount,
        LocalDateTime completedAt,
        InterviewReportStatus status,
        Integer averageAccuracy,
        String overallFeedback,
        List<InterviewReportItemResponseDto> strengths,
        List<InterviewReportItemResponseDto> weaknesses,
        List<InterviewQuestionResultDto> questions
) {
    public static InterviewReportResponseDto generating(InterviewReport report) {
        return new InterviewReportResponseDto(
                report.getInterview().getId(),
                report.getInterview().getCategory(),
                report.getInterview().getQuestionCount(),
                report.getInterview().getCompletedAt(),
                report.getStatus(),
                null,
                null,
                null,
                null,
                null
        );
    }

    public static InterviewReportResponseDto from(
            InterviewReport report,
            List<InterviewEvaluation> evaluations,
            List<InterviewReportItemResponseDto> strengths,
            List<InterviewReportItemResponseDto> weaknesses
    ) {

        List<InterviewQuestionResultDto> questions =
                evaluations.stream()
                        .map(InterviewQuestionResultDto::from)
                        .toList();

        return new InterviewReportResponseDto(
                report.getInterview().getId(),
                report.getInterview().getCategory(),
                report.getInterview().getQuestionCount(),
                report.getInterview().getCompletedAt(),
                report.getStatus(),
                report.getAverageAccuracy(),
                report.getOverallFeedback(),
                strengths,
                weaknesses,
                questions
        );
    }
}
