package com.example.rootin.interview.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewSessionTopic;
import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.interview.service.result.GeneratedQuestionResult;
import com.example.rootin.interview.repository.InterviewQuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewQuestionGenerationService { //gemini로 질문생성 및 답변평가

    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAiClient interviewAiClient;
    private final InterviewPromptFactory interviewPromptFactory;

    @Qualifier("interviewObjectMapper")
    private final ObjectMapper objectMapper;
    // 기본 질문 생성
    public GeneratedQuestionResult generateBasicQuestion(
            Interview interview,
            InterviewSessionTopic sessionTopic
    ) {
        InterviewTopic topic = sessionTopic.getInterviewTopic();
        List<String> usedQuestions = getUsedQuestionTexts(interview.getId());

        String prompt = interviewPromptFactory.buildBasicQuestionPrompt(interview, topic, usedQuestions);
        return generateQuestion(prompt, usedQuestions, topic.getKeywords());
    }
    // 꼬리 질문 생성
    public GeneratedQuestionResult generateFollowUpQuestion(
            Interview interview,
            InterviewSessionTopic sessionTopic,
            InterviewQuestion currentQuestion,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            String previousFeedback
    ) {
        InterviewTopic topic = sessionTopic.getInterviewTopic();
        List<String> usedQuestions = getUsedQuestionTexts(interview.getId());

        String prompt = interviewPromptFactory.buildFollowUpQuestionPrompt(
                interview,
                topic,
                currentQuestion,
                matchedKeywords,
                missingKeywords,
                previousFeedback,
                usedQuestions
        );
        return generateQuestion(prompt, usedQuestions, missingKeywords);
    }
    // 최종 평가 결과
    public AnswerEvaluationResult evaluateAnswer(
            InterviewQuestion question,
            String answerText
    ) {
        List<String> targetKeywords = question.getTargetKeywords() == null ? List.of() : question.getTargetKeywords();

        Map<String, Object> schema = buildEvaluationSchema(targetKeywords);
        String prompt = interviewPromptFactory.buildEvaluationPrompt(question, answerText, targetKeywords);
        String generatedJson = interviewAiClient.generate(prompt, schema);

        try {
            AnswerEvaluationPayload payload = objectMapper.readValue(generatedJson, AnswerEvaluationPayload.class);
            List<String> matchedKeywords = normalizeKeywordList(payload.matchedKeywords(), targetKeywords);
            List<String> missingKeywords = targetKeywords.stream()
                    .filter(keyword -> !matchedKeywords.contains(keyword))
                    .toList();
            double coverage = targetKeywords.isEmpty() // 키워드 기반 점수 계산
                    ? 100.0
                    : (matchedKeywords.size() * 100.0) / targetKeywords.size();

            return new AnswerEvaluationResult(
                    matchedKeywords,
                    missingKeywords,
                    roundScore(coverage),
                    clampScore(payload.score()),
                    payload.evaluation(),
                    payload.feedback()
            );
        } catch (JsonProcessingException e) {
            log.error("Gemini JSON parsing failed for questionId={}", question.getId(), e);
            throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
        }
    }
    //질문 생성 요청
    private GeneratedQuestionResult generateQuestion(
            String prompt,
            List<String> usedQuestions,
            List<String> allowedKeywords
    ) {
        String responseJson = interviewAiClient.generate(prompt, buildQuestionSchema());

        try {
            GeneratedQuestionPayload payload = objectMapper.readValue(responseJson, GeneratedQuestionPayload.class);
            String questionText = payload.questionText();
            if (questionText == null || questionText.isBlank()) {
                throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
            }

            String normalized = normalize(questionText);
            if (usedQuestions.stream().map(this::normalize).anyMatch(normalized::equals)) {
                throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
            }
            List<String> targetKeywords = normalizeKeywordList(payload.targetKeywords(),
                    allowedKeywords == null ? List.of() : allowedKeywords);
            if (targetKeywords.isEmpty()) {
                throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
            }
            return new GeneratedQuestionResult(questionText.trim(), targetKeywords);
        } catch (JsonProcessingException e) {
            log.error("Gemini JSON parsing failed for generated question", e);
            throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
        }
    }
    // 질문 스키마 생성
    private Map<String, Object> buildQuestionSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "questionText", Map.of("type", "string")
                        , "targetKeywords", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        )
                ),
                "required", List.of("questionText", "targetKeywords"),
                "additionalProperties", false
        );
    }
    // 평가 스키마 생성
    private Map<String, Object> buildEvaluationSchema(List<String> targetKeywords) {
        Map<String, Object> matchedKeywordItems = (targetKeywords == null || targetKeywords.isEmpty())
                ? Map.of("type", "string")
                : Map.of(
                "type", "string",
                "enum", targetKeywords
        );

        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "matchedKeywords", Map.of(
                                "type", "array",
                                "items", matchedKeywordItems
                        ),
                        "feedback", Map.of("type", "string")
                        , "score", Map.of("type", "integer", "minimum", 0, "maximum", 100)
                        , "evaluation", Map.of(
                                "type", "string",
                                "enum", List.of("CORRECT", "PARTIAL", "INCORRECT", "UNKNOWN")
                        )
                ),
                "required", List.of("matchedKeywords", "score", "evaluation", "feedback"),
                "additionalProperties", false
        );
    }

    private List<String> getUsedQuestionTexts(Long interviewId) {
        return interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interviewId)
                .stream()
                .map(InterviewQuestion::getQuestionText)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> normalizeKeywordList(List<String> matchedKeywords, List<String> targetKeywords) {
        if (matchedKeywords == null || matchedKeywords.isEmpty()) {
            return List.of();
        }

        Set<String> targetSet = targetKeywords.stream()
                .map(this::normalize)
                .collect(Collectors.toSet());

        return matchedKeywords.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(keyword -> targetSet.contains(normalize(keyword)))
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private double roundScore(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int clampScore(Integer score) {
        if (score == null) {
            throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
        }
        return Math.max(0, Math.min(100, score));
    }

    public record AnswerEvaluationResult(
            List<String> matchedKeywords,
            List<String> missingKeywords,
            Double keywordCoverage,
            Integer score,
            com.example.rootin.interview.domain.AnswerEvaluation evaluation,
            String feedback
    ) {
    }

    private record GeneratedQuestionPayload(String questionText, List<String> targetKeywords) {
    }

    private record AnswerEvaluationPayload(
            List<String> matchedKeywords,
            Integer score,
            com.example.rootin.interview.domain.AnswerEvaluation evaluation,
            String feedback
    ) {
    }
}
