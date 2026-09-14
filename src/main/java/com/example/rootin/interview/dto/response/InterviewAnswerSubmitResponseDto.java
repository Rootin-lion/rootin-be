package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.AnswerLevel;

public record InterviewAnswerSubmitResponseDto( //답변 제출 시 응답 구조
        Long interviewId,
        boolean topicCompleted,
        boolean interviewCompleted,
        AnswerLevel answerLevel,
        int accuracy,
        String feedback,
        InterviewQuestionResponseDto nextQuestion
) {
}
