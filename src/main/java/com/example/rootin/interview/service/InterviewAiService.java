package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.interview.dto.response.AnswerEvaluationResponseDto;
import com.example.rootin.interview.dto.response.GeneratedQuestionResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

@Service
public class InterviewAiService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final RestClient restClient;

    public InterviewAiService(
            ObjectMapper objectMapper,
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl
    ) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create(baseUrl);
    }

    public GeneratedQuestionResponseDto generateBasicQuestion(
            InterviewTopic topic
    ) {
        String prompt = """
                당신은 CS 기술 면접관입니다.
                다음 토픽을 기반으로 기본 면접 질문 하나를 생성하세요.

                토픽: %s
                핵심 키워드: %s
                기본 평가 기준: %s

                기본 질문은 해당 토픽의 핵심 개념을 사용자가 직접 설명하도록 만드는 질문이어야 합니다.
                다음 JSON 형식으로 응답하세요.
                {
                  "question": "질문",
                  "targetKeywords": "이번 질문에서 확인해야 하는 핵심 키워드",
                  "evaluationCriteria": "이번 질문의 구체적인 평가 기준"
                }
                """.formatted(
                        topic.getTopicName(),
                        topic.getKeywords(),
                        topic.getEvaluationCriteria()
        );
        String json = callGemini(prompt);

        try {
            return objectMapper.readValue(json, GeneratedQuestionResponseDto.class);
        }catch (Exception e){
            throw new IllegalArgumentException("질문 생성 응답 파싱에 실패했습니다.", e);
        }

    }

    public AnswerEvaluationResponseDto evaluateAnswer(
            InterviewQuestion question,
            String answer
    ) {
        String prompt = """
                당신은 CS 기술 면접 답변 평가자입니다.

                질문: %s
                평가 대상 키워드: %s
                평가 기준: %s
                사용자 답변: %s

                아래 기준으로 답변을 평가하세요.
                GOOD: 핵심 내용을 충분하고 정확하게 설명했습니다.
                AMBIGUOUS: 일부 개념은 알고 있지만 설명이 부족하거나 중요한 내용이 일부 누락되어 추가 확인이 필요합니다.
                UNKNOWN: 핵심 개념을 거의 설명하지 못했거나 대부분 잘못된 답변입니다.

                accuracy는 0~100 정수입니다.

                matchedKeywords는 단순히 단어가 등장했는지가 아니라 해당 개념을 의미상 올바르게 설명했는지를 판단하세요.
                missingKeywords는 질문에서 요구했지만 충분히 설명하지 않은 개념입니다.

                다음 JSON 형식으로 응답하세요.
                {
                  "level": "GOOD 또는 AMBIGUOUS 또는 UNKNOWN",
                  "accuracy": 0,
                  "feedback": "사용자에게 보여줄 피드백",
                  "matchedKeywords": "충분히 설명한 키워드",
                  "missingKeywords": "부족한 키워드"
                }
                """.formatted(
                        question.getQuestion(),
                        question.getTargetKeywords(),
                        question.getEvaluationCriteria(),
                        answer
        );
        String json = callGemini(prompt);

        try{
            return objectMapper.readValue(json, AnswerEvaluationResponseDto.class);
        }catch (Exception e){
            throw new IllegalArgumentException("답변 평가 응답 파싱에 실패했습니다.", e);
        }
    }


    public GeneratedQuestionResponseDto generateFollowUpQuestion(
            InterviewQuestion question,
            String answer,
            AnswerEvaluationResponseDto evaluation
    ) {
        String prompt = """
                당신은 CS 기술 면접관입니다.

                사용자의 답변에서 부족했던 내용을 확인하기 위한
                꼬리질문 하나를 생성하세요.

                이전 질문: %s
                사용자 답변: %s
                평가 피드백: %s
                부족한 키워드: %s

                규칙:
                - 이전 질문을 그대로 반복하지 않습니다.
                - 같은 토픽 안에서 질문합니다.
                - 부족했던 개념을 직접 확인합니다.
                - 새로운 토픽으로 넘어가지 않습니다.

                다음 JSON 형식으로 응답하세요.
                {
                  "question": "꼬리질문",
                  "targetKeywords": "이번 꼬리질문의 핵심 키워드",
                  "evaluationCriteria": "이번 꼬리질문의 평가 기준"
                }
                """
                .formatted(
                        question.getQuestion(),
                        answer,
                        evaluation.feedback(),
                        evaluation.missingKeywords()
                );

        String json = callGemini(prompt);

        try {
            return objectMapper.readValue(json, GeneratedQuestionResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("꼬리질문 생성 응답 파싱에 실패했습니다.", e);
        }
    }

    private String callGemini(String prompt){
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("temperature", 0.9, "responseMimeType", "application/json")
        );
        String response = callGeminiApi(requestBody);

        try {
            JsonNode root = objectMapper.readTree(response);

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            throw new IllegalStateException("Gemini 응답 처리에 실패했습니다.", e);
        }
    }
    //503 Service Unavailable일 때 최대 3회 시도
    private String callGeminiApi(Map<String, Object> requestBody) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return restClient
                        .post()
                        .uri(
                                "/models/{model}:generateContent?key={apiKey}",
                                model,
                                apiKey
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                if (attempt == maxAttempts) {
                    throw e;
                }

                try {
                    Thread.sleep(1_000L * (1L << (attempt - 1)));
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Gemini API 재시도 대기 중 요청이 중단되었습니다.", interruptedException);
                }
            }
        }

        throw new IllegalStateException("Gemini API 호출에 실패했습니다.");
    }
}
