package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewMode;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.member.domain.InterestField;

public record InterviewCreateResponseDto(
        Long interviewId,
        InterestField category,
        InterviewMode interviewMode,
        InterviewStatus status,
        int questionCount,
        InterviewQuestionResponseDto firstQuestion
) {

    public static InterviewCreateResponseDto from(Interview interview, InterviewQuestion firstQuestion) {
        return new InterviewCreateResponseDto(
                interview.getId(),
                interview.getCategory(),
                interview.getInterviewMode(),
                interview.getStatus(),
                interview.getQuestionCount(),
                InterviewQuestionResponseDto.from(firstQuestion)
        );
    }
}
