package com.example.rootin.interview.dto.socket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
//클라이언트가 서버로 보내는 답변 메시지
public record InterviewAnswerMessage(
        @NotNull(message = "questionId is required.")
        Long questionId,

        @NotBlank(message = "answerText is required.")
        String answerText,

        @NotBlank(message = "clientMessageId is required.")
        String clientMessageId
) {
}
