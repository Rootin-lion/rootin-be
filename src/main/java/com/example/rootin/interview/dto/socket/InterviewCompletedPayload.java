package com.example.rootin.interview.dto.socket;

import com.example.rootin.interview.dto.response.InterviewReportResponseDto;

public record InterviewCompletedPayload(
        InterviewReportResponseDto report
) {
}
