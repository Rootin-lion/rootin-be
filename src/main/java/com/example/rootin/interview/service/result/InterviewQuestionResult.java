package com.example.rootin.interview.service.result;

import com.example.rootin.interview.domain.InterviewQuestionType;
import com.example.rootin.interview.domain.AnswerEvaluation;

import java.util.List;

public record InterviewQuestionResult(
        Long questionId,
        Long topicId,
        String topicName,
        int topicOrder,
        int questionOrder,
        InterviewQuestionType questionType,
        String question,
        List<String> targetKeywords,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        Double keywordCoverage,
        Integer score,
        AnswerEvaluation evaluation,
        String answer,
        String feedback
) {
}
