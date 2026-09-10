package com.example.rootin.interview.dto.socket;

import java.time.LocalDateTime;
//모든 서버 웹소켓 메시지를 감싸는 공통 구조
public record InterviewSocketMessage<T>(
        InterviewEventType type,
        Long interviewId,
        String messageId,
        String clientMessageId,
        T data,
        LocalDateTime sentAt
) {
    public static <T> InterviewSocketMessage<T> of(
            InterviewEventType type,
            Long interviewId,
            String clientMessageId,
            T data
    ) {
        return new InterviewSocketMessage<>(
                type,
                interviewId,
                java.util.UUID.randomUUID().toString(),
                clientMessageId,
                data,
                LocalDateTime.now()
        );
    }
}
