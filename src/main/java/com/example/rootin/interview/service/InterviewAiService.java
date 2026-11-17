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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InterviewAiService {

    private static final Map<String, Object> QUESTION_RESPONSE_FORMAT = jsonSchemaFormat(
            "interview_question",
            Map.of(
                    "question", Map.of("type", "string"),
                    "targetKeywords", Map.of("type", "string"),
                    "evaluationCriteria", Map.of("type", "string")
            ),
            List.of("question", "targetKeywords", "evaluationCriteria")
    );

    private static final Map<String, Object> EVALUATION_RESPONSE_FORMAT = jsonSchemaFormat(
            "answer_evaluation",
            Map.of(
                    "level", Map.of("type", "string", "enum", List.of("GOOD", "AMBIGUOUS", "UNKNOWN")),
                    "accuracy", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                    "feedback", Map.of("type", "string"),
                    "matchedKeywords", Map.of("type", "string"),
                    "missingKeywords", Map.of("type", "string")
            ),
            List.of("level", "accuracy", "feedback", "matchedKeywords", "missingKeywords")
    );

    private static final Map<String, Object> REPORT_RESPONSE_FORMAT = Map.of(
            "type", "json_schema",
            "name", "interview_report",
            "strict", true,
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "overallFeedback", Map.of("type", "string"),
                            "strengths", reportItemsSchema(),
                            "weaknesses", reportItemsSchema()
                    ),
                    "required", List.of("overallFeedback", "strengths", "weaknesses"),
                    "additionalProperties", false
            )
    );

    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestClient restClient;

    public InterviewAiService(
            ObjectMapper objectMapper,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.connect-timeout:10s}") Duration connectTimeout,
            @Value("${openai.read-timeout:120s}") Duration readTimeout
    ) {
        this.objectMapper = objectMapper;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public GeneratedQuestionResponseDto generateBasicQuestion(
            InterviewTopic topic
    ) {
        String prompt = """
                당신은 신입 개발자를 대상으로 CS 기술면접을 진행하는 면접관입니다.
                다음 토픽을 기반으로 기본 면접 질문 하나를 생성하세요.
                
                토픽: %s
                핵심 키워드: %s
                기본 평가 기준: %s
                
                [질문 생성 원칙]
                1. 질문은 신입 개발자 기술면접에서 일반적으로 등장할 수 있는
                   기본 개념 수준으로 생성합니다.
                
                2. 제공된 핵심 키워드와 평가 기준은 해당 토픽의 전체 학습 범위입니다.
                   모든 키워드를 한 번의 질문에서 확인할 필요는 없습니다.
                
                3. 토픽에서 가장 중요한 핵심 개념 1~2개를 중심으로 질문합니다.
                   서로 밀접하게 연결된 개념은 함께 질문할 수 있습니다.
                
                4. 하나의 질문에서 정의, 원리, 내부 동작, 차이점, 응용 사례를
                   모두 설명하도록 요구하지 않습니다.
                
                5. 사용자가 자신의 언어로 개념을 설명할 수 있도록
                   자연스럽고 간결한 질문을 생성합니다.
                
                6. 세부 구현 방식이나 예외 상황은 기본 질문에서
                   반드시 확인해야 하는 내용이 아닙니다.
                
                [평가 기준 생성 원칙]
                1. targetKeywords에는 이번 질문에서 실제로 확인할
                   핵심 키워드만 포함합니다.
                
                2. evaluationCriteria에는 해당 질문에 대한 답변에서
                   반드시 확인해야 하는 핵심 내용만 작성합니다.
                
                3. 질문에서 직접 요구하지 않은 심화 개념을
                   필수 평가 기준에 포함하지 않습니다.
                
                4. 핵심 개념을 올바르게 설명했다면
                   충분한 답변으로 인정할 수 있도록 기준을 작성합니다.
                
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
        String json = callOpenAi(prompt, QUESTION_RESPONSE_FORMAT);

        try {
            return objectMapper.readValue(json, GeneratedQuestionResponseDto.class);
        }catch (Exception e){
            log.warn("OpenAI 질문 응답 변환 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
        }

    }

    public AnswerEvaluationResponseDto evaluateAnswer(
            InterviewQuestion question,
            String answer
    ) {
        String prompt = """
                당신은 신입 개발자의 CS 기술면접 답변을 평가하는 면접관입니다.
                
                질문: %s
                평가 대상 키워드: %s
                평가 기준: %s
                사용자 답변: %s
                
                [평가 기본 원칙]
                1. 질문에서 직접 요구한 핵심 개념을 중심으로 평가합니다.
                
                2. 사용자가 핵심 개념을 올바르게 이해하고 설명했다면 전문 용어를 정확히 사용하지 않았더라도 의미를 인정합니다.
                
                3. 답변이 간결하다는 이유만으로 감점하지 않습니다. 다만 질문에서 요구한 핵심 설명이 빠졌다면 감점할 수 있습니다.
                
                4. 질문에서 요구하지 않은 심화 개념이나 세부 구현 방식을 설명하지 않았다는 이유로 감점하지 않습니다.
                
                5. 핵심 개념의 명백한 오류와 단순한 설명 부족을 구분합니다.
                
                6. 평가 대상 키워드는 참고 기준이며, 키워드의 단순 등장 여부보다 개념의 이해도를 중요하게 평가합니다.
                   
                7. 현재 제공된 평가 대상 키워드(targetKeywords)와 평가 기준(evaluationCriteria)은 이번 질문에 대한 평가 범위입니다.
                   토픽 전체의 핵심 키워드나 학습 범위를 추측하여 추가적인 평가 기준을 적용하지 마세요.
                
                   이번 질문에서 요구하지 않은 개념을 설명하지 않았다는 이유로 감점하지 마세요.   
                                   
                [정확도 평가 기준]
                다음 세 가지 항목을 종합하여 accuracy를 0~100으로 평가합니다.
                
                - 핵심 개념 이해도 (50점)
                  질문의 핵심 원리와 목적을 올바르게 이해했는가?
                
                - 기술적 정확성 (30점)
                  설명에 중요한 개념 오류나 사실관계의 오류가 없는가?
                
                - 설명의 충실도 (20점)
                  질문에서 직접 요구한 내용을 충분히 설명했는가?
                
                [점수 해석]
                90~100점:
                핵심 내용을 정확하고 충분히 설명했으며,
                중요한 개념 오류가 없는 답변입니다.
                
                75~89점:
                핵심 개념을 올바르게 이해하고 있으나
                설명 일부가 부족하거나 부정확한 부분이 있는 답변입니다.
                
                50~74점:
                기본 개념을 일부 이해하고 있으나
                중요한 설명이 누락되거나 일부 개념이 부정확한 답변입니다.
                
                25~49점:
                핵심 개념의 이해가 부족하거나
                중요한 개념 오류가 포함된 답변입니다.
                
                0~24점:
                질문과 관련된 핵심 내용을 거의 설명하지 못했거나
                대부분 잘못 이해한 답변입니다.
                
                [답변 수준 분류]
                GOOD:
                핵심 내용을 충분하고 정확하게 설명한 답변입니다.
                사소한 설명 부족은 허용합니다.
                
                AMBIGUOUS:
                핵심 개념을 일부 이해하고 있으나
                중요한 설명이 부족하거나 추가 확인이 필요한 답변입니다.
                
                UNKNOWN:
                핵심 개념을 거의 설명하지 못했거나
                중요한 내용을 대부분 잘못 이해한 답변입니다.
                
                level은 accuracy뿐 아니라 실제 핵심 개념 이해도를
                함께 고려하여 판단합니다.
                
                [키워드 분류]
                matchedKeywords:
                - 개념을 의미상 올바르게 설명한 항목입니다.
                - 정확한 전문 용어가 없어도 의미가 같으면 인정합니다.
                
                missingKeywords:
                - 이번 질문에서 요구했지만 충분히 설명하지 못한 핵심 개념입니다.
                - 단순히 심화 설명이 부족하다는 이유로 포함하지 않습니다.
                - 충분히 설명한 개념은 포함하지 않습니다.
                - 부족한 부분을 구체적으로 표현합니다.
                
                [피드백 작성]
                1. 정확하게 설명한 내용을 먼저 언급합니다.
                2. 중요한 오류나 부족한 개념을 1~2개 중심으로 설명합니다.
                3. 질문에서 요구하지 않은 심화 내용을 과도하게 나열하지 않습니다.
                4. 사용자가 다음 답변에서 개선할 수 있도록 작성합니다.
                5. 피드백은 2~4문장으로 간결하게 작성합니다.
                
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
        String json = callOpenAi(prompt, EVALUATION_RESPONSE_FORMAT);

        try{
            return objectMapper.readValue(json, AnswerEvaluationResponseDto.class);
        }catch (Exception e){
            log.warn("OpenAI 답변 평가 응답 변환 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
        }
    }


    public GeneratedQuestionResponseDto generateFollowUpQuestion(
            InterviewQuestion question,
            String answer,
            AnswerEvaluationResponseDto evaluation
    ) {
        String prompt = """
                당신은 신입 개발자를 대상으로 CS 기술면접을 진행하는 면접관입니다.
                
                사용자의 이전 답변을 바탕으로
                이해도를 추가로 확인하기 위한 꼬리질문 하나를 생성하세요.
                
                이전 질문: %s
                사용자 답변: %s
                평가 피드백: %s
                부족한 키워드: %s
                
                [꼬리질문 생성 원칙]
                1. 이전 질문과 동일한 토픽 안에서 질문합니다.
                
                2. 이전 질문을 그대로 반복하지 않습니다.
                
                3. 부족한 개념이 여러 개라면
                   가장 중요하거나 이해도 확인에 필요한 개념 하나를 선택합니다.
                
                4. 부족했던 모든 키워드를
                   하나의 꼬리질문에서 확인하려고 하지 않습니다.
                
                5. 이전 답변의 이해도에 따라 질문 난이도를 조절합니다.
                
                   - 핵심 개념을 잘못 이해한 경우:
                     해당 개념을 바로잡을 수 있는 간단한 질문
                
                   - 핵심 개념을 일부 이해한 경우:
                     부족한 부분을 구체적으로 확인하는 질문
                
                   - 핵심 개념을 충분히 이해한 경우:
                     간단한 비교나 실제 적용 상황을 묻는 질문
                
                6. 꼬리질문은 기본 질문보다 반드시 어려울 필요가 없습니다.
                
                7. 하나의 질문에 여러 개의 독립적인 요구사항을 포함하지 않습니다.
                
                8. 신입 개발자 기술면접 수준에서
                   자연스럽게 답변할 수 있는 질문을 생성합니다.
                
                [평가 기준 생성 원칙]
                1. targetKeywords에는 이번 꼬리질문에서
                   실제로 확인할 핵심 개념만 포함합니다.
                
                2. evaluationCriteria에는 질문에서 직접 요구한
                   핵심 내용의 이해 여부를 평가하도록 작성합니다.
                
                3. 질문에서 요구하지 않은 심화 개념이나
                   세부 구현 방식은 필수 평가 기준에 포함하지 않습니다.
                
                4. 이전 답변에서 부족했던 개념을
                   올바르게 보완했는지 확인할 수 있도록 작성합니다.
                
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

        String json = callOpenAi(prompt, QUESTION_RESPONSE_FORMAT);

        try {
            return objectMapper.readValue(json, GeneratedQuestionResponseDto.class);
        } catch (Exception e) {
            log.warn("OpenAI 꼬리질문 응답 변환 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
        }
    }

    private String callOpenAi(String prompt, Map<String, Object> responseFormat){
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", List.of(Map.of("role", "user", "content", prompt)),
                "reasoning", Map.of("effort", "low"),
                "text", Map.of("format", responseFormat),
                "store", false
        );
        String response = callOpenAiApi(requestBody);

        try {
            JsonNode root = objectMapper.readTree(response);

            for (JsonNode item : root.path("output")) {
                if (!"message".equals(item.path("type").asText())) {
                    continue;
                }

                for (JsonNode content : item.path("content")) {
                    if ("output_text".equals(content.path("type").asText())) {
                        return content.path("text").asText();
                    }
                }
            }

        } catch (Exception e) {
            log.warn("OpenAI Responses API 응답 파싱 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
        }

        log.warn("OpenAI Responses API 응답에 output_text가 없습니다. model={}", model);
        throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
    }

    private String callOpenAiApi(Map<String, Object> requestBody) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return restClient
                        .post()
                        .uri("/responses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);
            } catch (HttpStatusCodeException e) {
                boolean retryable = e.getStatusCode().value() == 429
                        || e.getStatusCode().is5xxServerError();

                log.warn(
                        "OpenAI API 호출 실패: status={}, attempt={}/{}, response={}",
                        e.getStatusCode().value(),
                        attempt,
                        maxAttempts,
                        abbreviate(e.getResponseBodyAsString())
                );

                if (!retryable || attempt == maxAttempts) {
                    throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
                }

                waitBeforeRetry(attempt);
            } catch (RestClientException e) {
                log.warn(
                        "OpenAI API 통신 실패: attempt={}/{}, error={}",
                        attempt,
                        maxAttempts,
                        e.getMessage()
                );

                if (attempt == maxAttempts) {
                    throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
                }

                waitBeforeRetry(attempt);
            }
        }

        throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(1_000L * (1L << (attempt - 1)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERVIEW_AI_SERVICE_ERROR);
        }
    }

    private String abbreviate(String value) {
        int maxLength = 1_000;
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    private static Map<String, Object> jsonSchemaFormat(
            String name,
            Map<String, Object> properties,
            List<String> required
    ) {
        return Map.of(
                "type", "json_schema",
                "name", name,
                "strict", true,
                "schema", Map.of(
                        "type", "object",
                        "properties", properties,
                        "required", required,
                        "additionalProperties", false
                )
        );
    }

    private static Map<String, Object> reportItemsSchema() {
        return Map.of(
                "type", "array",
                "items", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "title", Map.of("type", "string"),
                                "content", Map.of("type", "string")
                        ),
                        "required", List.of("title", "content"),
                        "additionalProperties", false
                )
        );
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
                당신은 신입 개발자의 CS 기술면접 결과를 분석하는 평가자입니다.
                
                아래는 한 사용자의 전체 면접 결과입니다.
                %s
                
                전체 면접을 종합하여 다음 내용을 작성하세요.
                
                [분석 원칙]
                1. 기본 질문과 꼬리질문을 함께 고려하여 분석합니다.
                
                2. 기본 질문에서 부족했던 개념을
                   꼬리질문에서 정확하게 설명했다면
                   이를 긍정적인 개선으로 평가합니다.
                
                3. 꼬리질문에서 이미 보완한 개념을
                   여전히 이해하지 못하는 약점으로 단정하지 않습니다.
                
                4. 단순히 키워드의 누락 여부가 아니라
                   실제 답변에서 드러난 개념 이해도를 바탕으로 분석합니다.
                
                5. 단 한 번의 사소한 설명 부족보다
                   중요하거나 반복적으로 나타난 개념 오류를 우선합니다.
                
                6. 실제 답변에서 확인되지 않은 역량을
                   임의로 강점이나 약점으로 작성하지 않습니다.
                
                [overallFeedback]
                - 전체 면접 결과에 대한 종합 피드백
                - 잘 이해하고 있는 부분과 보완할 부분을 요약
                - 꼬리질문에서 확인된 개선 과정도 고려
                - 세부 키워드를 과도하게 나열하지 않음
                - strengths, weaknesses의 내용을 그대로 반복하지 않음
                - 2~3문장, 150자 이내
                
                [strengths]
                - 면접 전체에서 잘한 부분을 2~3개 선정
                - 각 항목을 title과 content로 구분
                - title은 잘한 핵심 개념을 짧게 작성
                - content는 잘한 이유를 1문장으로 작성
                - 꼬리질문에서 정확하게 보완한 개념도 강점으로 인정 가능
                - 각 content는 50자 내외
                - 유사한 내용은 중복하지 않음
                
                [weaknesses]
                - 면접 전체에서 보완이 필요한 부분을 1~3개 선정
                - 각 항목을 title과 content로 구분
                - title은 보완이 필요한 핵심 개념을 짧게 작성
                - content는 부족한 점 또는 보완 방향을 1문장으로 작성
                - 이후 답변에서 보완된 개념은 약점으로 단정하지 않음
                - 실제로 이해가 부족했던 핵심 개념을 우선 선정
                - 각 content는 50자 내외
                - 유사한 내용은 중복하지 않음
                
                반드시 다음 JSON 형식으로 응답하세요.
                {
                  "overallFeedback": "...",
                  "strengths": [
                    {
                      "title": "핵심 강점 제목",
                      "content": "강점에 대한 구체적인 설명"
                    }
                  ],
                  "weaknesses": [
                    {
                      "title": "보완점 제목",
                      "content": "보완이 필요한 이유와 방향"
                    }
                  ]
                }
                """.formatted(interviewContent);

        String json = callOpenAi(prompt, REPORT_RESPONSE_FORMAT);

        try {
            return objectMapper.readValue(json, GeneratedReportResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("리포트 AI 응답 파싱에 실패했습니다.", e);
        }
    }
}
