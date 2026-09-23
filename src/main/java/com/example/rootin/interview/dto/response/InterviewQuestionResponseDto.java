package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.QuestionType;

public record InterviewQuestionResponseDto (
        Long questionId,
        Long topicId,
        String topicName,
        int questionOrder,
        QuestionType questionType,
        String question
){
    public static InterviewQuestionResponseDto from(InterviewQuestion question) {
        return new InterviewQuestionResponseDto(
                question.getId(),
                question.getTopic().getId(),
                question.getTopic().getTopicName(),
                question.getQuestionOrder(),
                question.getQuestionType(),
                question.getQuestion()
        );
    }
}
