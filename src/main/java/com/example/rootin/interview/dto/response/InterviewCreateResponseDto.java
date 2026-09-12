package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.member.domain.InterestField;

public record InterviewCreateResponseDto(
        Long interviewId,
        InterestField category,
        InterviewStatus status,
        int totalTopicCount
) {

    public static InterviewCreateResponseDto from(Interview interview) {
        return new InterviewCreateResponseDto(
                interview.getId(),
                interview.getCategory(),
                interview.getStatus(),
                interview.getQuestionCount()
        );
    }
}
