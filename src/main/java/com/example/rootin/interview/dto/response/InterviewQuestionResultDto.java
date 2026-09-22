package com.example.rootin.interview.dto.response;

import com.example.rootin.interview.domain.InterviewEvaluation;

//문항별 상세 피드백
public record InterviewQuestionResultDto(
        Long questionId,
        int questionOrder,
        String topicName,
        String question,
        String answer,
        int accuracy,
        String feedback,
        String missingKeywords

) {

    public static InterviewQuestionResultDto from(
            InterviewEvaluation evaluation
    ) {

        var answer = evaluation.getAnswer();
        var question = answer.getQuestion();

        return new InterviewQuestionResultDto(
                question.getId(),
                question.getQuestionOrder(),
                question.getTopic().getTopicName(),
                question.getQuestion(),
                answer.getAnswer(),
                evaluation.getAccuracy(),
                evaluation.getFeedback(),
                evaluation.getMissingKeywords()
        );
    }
}
