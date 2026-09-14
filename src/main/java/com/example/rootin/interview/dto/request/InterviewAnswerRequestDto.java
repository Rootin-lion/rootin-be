package com.example.rootin.interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InterviewAnswerRequestDto {
    @NotNull
    private Long questionId;

    @NotBlank
    private String answer;
}
