package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewQuestionType;

import java.util.List;

public record InterviewQuestionDto(
        Long questionId,
        Long topicId,
        String topicName,
        int topicOrder,
        int questionOrder,
        InterviewQuestionType questionType,
        String question,
        List<String> targetKeywords
) {
}
