package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.AnswerEvaluation;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewQuestionType;
import com.example.rootin.interview.repository.InterviewQuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class InterviewQuestionGenerationServiceTest {

    @Test
    void evaluatesSemanticScoreSeparatelyFromKeywordCoverage() {
        InterviewAiClient aiClient = (prompt, schema) -> """
                {
                  "matchedKeywords": ["비선점", "순환대기"],
                  "score": 30,
                  "evaluation": "INCORRECT",
                  "feedback": "용어는 언급했지만 개념 설명이 정확하지 않습니다."
                }
                """;
        InterviewQuestionGenerationService service = serviceWith(aiClient);
        InterviewQuestion question = InterviewQuestion.create(
                null, null, null, InterviewQuestionType.BASIC, 1,
                "비선점과 순환대기를 설명해주세요.", List.of("비선점", "순환대기"));

        var result = service.evaluateAnswer(question, "비선점은 비선점이고 순환대기는 순환 대기입니다.");

        assertThat(result.keywordCoverage()).isEqualTo(100.0);
        assertThat(result.score()).isEqualTo(30);
        assertThat(result.evaluation()).isEqualTo(AnswerEvaluation.INCORRECT);
    }

    @Test
    void missingKeywordsAreLimitedToCurrentQuestionTargets() {
        InterviewAiClient aiClient = (prompt, schema) -> """
                {
                  "matchedKeywords": [],
                  "score": 5,
                  "evaluation": "UNKNOWN",
                  "feedback": "개념을 모른다고 답했습니다."
                }
                """;
        InterviewQuestionGenerationService service = serviceWith(aiClient);
        InterviewQuestion question = InterviewQuestion.create(
                null, null, null, InterviewQuestionType.FOLLOW_UP, 2,
                "비선점과 순환대기를 설명해주세요.", List.of("비선점", "순환대기"));

        var result = service.evaluateAnswer(question, "비선점은 잘 모르겠습니다.");

        assertThat(result.missingKeywords()).containsExactly("비선점", "순환대기");
        assertThat(result.missingKeywords()).doesNotContain("상호배제", "점유대기");
        assertThat(result.evaluation()).isEqualTo(AnswerEvaluation.UNKNOWN);
    }

    private InterviewQuestionGenerationService serviceWith(InterviewAiClient aiClient) {
        return new InterviewQuestionGenerationService(
                mock(InterviewQuestionRepository.class),
                aiClient,
                new InterviewPromptFactory(),
                new ObjectMapper()
        );
    }
}
