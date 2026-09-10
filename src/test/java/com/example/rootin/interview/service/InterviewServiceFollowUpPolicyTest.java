package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.AnswerEvaluation;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewQuestionType;
import com.example.rootin.interview.domain.InterviewSessionTopic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InterviewServiceFollowUpPolicyTest {

    @Test
    void allowsOnlyOneFollowUpForPartialBasicAnswer() {
        InterviewService service = service();
        InterviewSessionTopic sessionTopic = InterviewSessionTopic.create(null, null, 1);
        InterviewQuestion basic = question(InterviewQuestionType.BASIC);
        var partial = evaluation(AnswerEvaluation.PARTIAL);

        assertThat(service.shouldAskFollowUp(basic, sessionTopic, partial)).isTrue();

        sessionTopic.incrementFollowUpQuestionCount();
        assertThat(service.shouldAskFollowUp(basic, sessionTopic, partial)).isFalse();
    }

    @Test
    void neverRepeatsAFollowUpOrUnknownAnswer() {
        InterviewService service = service();
        InterviewSessionTopic sessionTopic = InterviewSessionTopic.create(null, null, 1);

        assertThat(service.shouldAskFollowUp(question(InterviewQuestionType.FOLLOW_UP), sessionTopic,
                evaluation(AnswerEvaluation.PARTIAL))).isFalse();
        assertThat(service.shouldAskFollowUp(question(InterviewQuestionType.BASIC), sessionTopic,
                evaluation(AnswerEvaluation.UNKNOWN))).isFalse();
    }

    private InterviewQuestion question(InterviewQuestionType type) {
        return InterviewQuestion.create(null, null, null, type, 1, "질문", List.of("비선점"));
    }

    private InterviewQuestionGenerationService.AnswerEvaluationResult evaluation(AnswerEvaluation evaluation) {
        return new InterviewQuestionGenerationService.AnswerEvaluationResult(
                List.of(), List.of("비선점"), 0.0, 10, evaluation, "feedback");
    }

    private InterviewService service() {
        return new InterviewService(
                mock(com.example.rootin.interview.repository.InterviewRepository.class),
                mock(com.example.rootin.interview.repository.InterviewTopicRepository.class),
                mock(com.example.rootin.interview.repository.InterviewSessionTopicRepository.class),
                mock(com.example.rootin.interview.repository.InterviewQuestionRepository.class),
                mock(com.example.rootin.interview.repository.InterviewAnswerRepository.class),
                mock(InterviewQuestionGenerationService.class)
        );
    }
}
