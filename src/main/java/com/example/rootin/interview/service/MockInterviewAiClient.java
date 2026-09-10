package com.example.rootin.interview.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@ConditionalOnProperty(name = "gemini.mock-enabled", havingValue = "true")
public class MockInterviewAiClient implements InterviewAiClient {

    private final AtomicLong questionSequence = new AtomicLong();

    @Override
    public String generate(String prompt, Map<String, Object> schema) {
        if (isQuestionSchema(schema)) {
            long sequence = questionSequence.incrementAndGet();
            return "{\"questionText\":\"Mock interview question " + sequence + "\"}";
        }

        return """
                {
                  "matchedKeywords": [],
                  "feedback": "Mock feedback: the WebSocket interview flow is working."
                }
                """;
    }

    private boolean isQuestionSchema(Map<String, Object> schema) {
        Object properties = schema.get("properties");
        return properties instanceof Map<?, ?> propertyMap
                && propertyMap.containsKey("questionText");
    }
}
