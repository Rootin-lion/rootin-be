package com.example.rootin.interview.dto.response;

import java.util.List;

//리포트 상단 부분 생성
public record GeneratedReportResponseDto(
        String overallFeedback,
        List<InterviewReportItemResponseDto> strengths,
        List<InterviewReportItemResponseDto> weaknesses
) {
}
