package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.InterviewAnswer;
import com.example.rootin.interview.domain.InterviewEvaluation;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.interview.dto.response.AnswerEvaluationResponseDto;
import com.example.rootin.interview.dto.response.GeneratedQuestionResponseDto;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.dto.response.GeneratedReportResponseDto;
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
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
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
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
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
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
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
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
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
                    throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
                }

                try {
                    Thread.sleep(1_000L * (1L << (attempt - 1)));
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
                }
            }
        }

        throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
    }

    public GeneratedReportResponseDto generateReport(List<InterviewEvaluation> evaluations){
        StringBuilder interviewContent = new StringBuilder();

        for(InterviewEvaluation evaluation : evaluations){
            InterviewAnswer answer = evaluation.getAnswer();
            InterviewQuestion question = answer.getQuestion();
            interviewContent.append("""
                [질문 %d]
                토픽: %s
                질문: %s
                사용자 답변: %s
                정확도: %d
                평가: %s
                피드백: %s
                부족한 키워드: %s
                """.formatted(
                    question.getQuestionOrder(),
                    question.getTopic().getTopicName(),
                    question.getQuestion(),
                    answer.getAnswer(),
                    evaluation.getAccuracy(),
                    evaluation.getAnswerLevel(),
                    evaluation.getFeedback(),
                    evaluation.getMissingKeywords()
            ));
        }

        String prompt = """
            당신은 CS 기술 면접 결과 분석가입니다.
            아래는 한 사용자의 전체 면접 결과입니다.
            %s
            전체 면접을 종합하여 다음 내용을 작성하세요.

            overallFeedback:
            - 전체 면접 결과에 대한 종합 피드백
            - 사용자가 잘 이해하고 있는 부분과 보완해야 할 부분을 핵심만 요약
            - 세부 개념이나 키워드를 과도하게 나열하지 않음
            - strengths, weaknesses의 내용을 그대로 반복하지 않음
            - 2~3문장, 150자 이내로 간결하게 작성

            strengths:
            - 면접 전체에서 잘한 부분을 2~3개 선정
            - 각 항목을 title과 content로 구분
            - title은 잘한 핵심 개념을 짧게 작성
            - content는 잘한 이유를 1문장으로 간결하게 작성
            - 각 content는 50자 내외로 작성
            - 동일하거나 유사한 내용을 중복하지 않음

            weaknesses:
            - 면접 전체에서 보완이 필요한 부분을 1~3개 선정
            - 각 항목을 title과 content로 구분
            - title은 보완이 필요한 핵심 개념을 짧게 작성
            - content는 부족한 점 또는 보완 방향을 1문장으로 간결하게 작성
            - 각 content는 50자 내외로 작성
            - 동일하거나 유사한 내용을 중복하지 않음

            반드시 다음 JSON 형식으로 응답하세요.
            {
              "overallFeedback": "...",
              "strengths": [
                { "title": "핵심 강점 제목", "content": "강점에 대한 구체적인 설명" }
              ],
              "weaknesses": [
                { "title": "보완점 제목", "content": "보완이 필요한 이유와 방향" }
              ]
            }
            """.formatted(interviewContent);

        String json = callGemini(prompt);

        try {
            return objectMapper.readValue(json, GeneratedReportResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("리포트 AI 응답 파싱에 실패했습니다.", e);
        }
    }
}
