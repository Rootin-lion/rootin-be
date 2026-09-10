package com.example.rootin.interview.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = "gemini.mock-enabled", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class GeminiInterviewClient implements InterviewAiClient {

    @Qualifier("interviewObjectMapper")
    private final ObjectMapper objectMapper;

    @Qualifier("geminiWebClient")
    private final WebClient webClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.model:gemini-3.6-flash}")
    private String model;

    @Value("${gemini.request-timeout-ms:120000}")
    private long requestTimeoutMs;

    @Override
    public String generate(String prompt, Map<String, Object> schema) {
        ensureApiKey();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", prompt);
        body.put("response_format", Map.of(
                "type", "text",
                "mime_type", "application/json",
                "schema", schema
        ));

        String responseBody;
        try {
            responseBody = webClient.post()
                    .uri(baseUrl + "/interactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(String.class);
                        }

                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(bodyText -> {
                                    log.error(
                                            "Gemini request failed. status={}, body={}",
                                            response.statusCode().value(),
                                            abbreviate(bodyText)
                                    );
                                    return Mono.error(mapStatus(response.statusCode().value()));
                                });
                    })
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .maxBackoff(Duration.ofSeconds(2))
                            .filter(this::isRetryableServerError)
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .timeout(Duration.ofMillis(requestTimeoutMs))
                    .block();
        } catch (Exception e) {
            if (isTimeout(e)) {
                throw new CustomException(ErrorCode.GEMINI_TIMEOUT);
            }
            throw e;
        }

        if (responseBody == null || responseBody.isBlank()) {
            log.error("Gemini returned an empty response body.");
            throw new CustomException(ErrorCode.GEMINI_SERVER_ERROR);
        }

        return extractGeneratedText(responseBody);
    }

    private String extractGeneratedText(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<>() {
            });
            Object outputText = responseMap.get("output_text");
            if (outputText instanceof String text && !text.isBlank()) {
                return text;
            }

            Object steps = responseMap.get("steps");
            if (steps instanceof List<?> stepList) {
                for (Object step : stepList) {
                    if (!(step instanceof Map<?, ?> stepMap)) {
                        continue;
                    }
                    Object content = stepMap.get("content");
                    if (content instanceof List<?> contentList) {
                        for (Object part : contentList) {
                            if (!(part instanceof Map<?, ?> partMap)) {
                                continue;
                            }
                            Object text = partMap.get("text");
                            if (text instanceof String partText && !partText.isBlank()) {
                                return partText;
                            }
                        }
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Gemini response parsing failed", e);
            throw new CustomException(ErrorCode.GEMINI_JSON_PARSE_ERROR);
        }

        log.error("Gemini response did not contain output text. body={}", abbreviate(responseBody));
        throw new CustomException(ErrorCode.GEMINI_SERVER_ERROR);
    }

    private RuntimeException mapStatus(int status) {
        if (status == 429) {
            return new CustomException(ErrorCode.GEMINI_RATE_LIMITED);
        }
        if (status >= 500) {
            return new CustomException(ErrorCode.GEMINI_SERVER_ERROR);
        }
        return new CustomException(ErrorCode.GEMINI_BAD_REQUEST);
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isRetryableServerError(Throwable throwable) {
        return throwable instanceof CustomException customException
                && customException.getErrorCode() == ErrorCode.GEMINI_SERVER_ERROR;
    }

    private String abbreviate(String value) {
        if (value == null || value.length() <= 2000) {
            return value;
        }
        return value.substring(0, 2000) + "...";
    }

    private void ensureApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
    }
}
