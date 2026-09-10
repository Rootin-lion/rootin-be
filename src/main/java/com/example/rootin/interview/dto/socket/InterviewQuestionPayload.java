package com.example.rootin.interview.dto.socket;

import com.example.rootin.interview.domain.InterviewQuestionType;

public record InterviewQuestionPayload(
        Long questionId,
        Long topicId,
        String topicName,
        int topicOrder,
        int questionOrder,
        InterviewQuestionType questionType,
        String content,
        Long parentQuestionId
) {
}
