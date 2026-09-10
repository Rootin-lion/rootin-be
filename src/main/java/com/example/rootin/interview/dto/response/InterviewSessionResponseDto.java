package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewMode;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewSessionResponseDto(
        Long interviewId,
        InterestField field,
        int questionCount,
        InterviewMode interviewMode,
        InterviewStatus status,
        LocalDateTime startedAt,
        List<InterviewQuestionDto> questions
) {
}
