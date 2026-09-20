package com.example.rootin.interview.dto.response;

// '잘하고 있어요', '보완이 필요해요' 리스트 반환을 위한
public record InterviewReportItemResponseDto(
        String title,
        String content
) {
}
