package com.example.rootin.interview.service.result;

import com.example.rootin.interview.dto.response.InterviewReportResponseDto;

public record InterviewAnswerResult(
        Long interviewId,
        Long answerId,
        boolean topicCompleted,
        boolean interviewCompleted,
        InterviewQuestionResult evaluatedQuestion,
        InterviewQuestionResult nextQuestion,
        InterviewReportResponseDto report
) {
}
