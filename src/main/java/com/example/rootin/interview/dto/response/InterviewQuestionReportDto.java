package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.AnswerEvaluation;
import com.example.rootin.interview.domain.InterviewQuestionType;

import java.util.List;

public record InterviewQuestionReportDto(
        Long questionId,
        Long topicId,
        String topicName,
        int questionOrder,
        InterviewQuestionType questionType,
        String question,
        String answer,
        String feedback,
        List<String> targetKeywords,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        Double keywordCoverage,
        Integer score,
        AnswerEvaluation evaluation
) {
}
