package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewReport;
import com.example.rootin.interview.domain.InterviewReportStatus;

// 리포트 생성 POST 응답
public record InterviewReportCreateResponseDto(
        Long interviewId,
        InterviewReportStatus status
) {
    public static InterviewReportCreateResponseDto from(InterviewReport report) {
        return new InterviewReportCreateResponseDto(
                report.getInterview().getId(),
                report.getStatus()
        );
    }
}

