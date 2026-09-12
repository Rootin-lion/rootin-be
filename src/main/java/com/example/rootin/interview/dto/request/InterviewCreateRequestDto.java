package com.example.rootin.interview.dto.request;

import com.example.rootin.interview.domain.InterviewMode;
import com.example.rootin.member.domain.InterestField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InterviewCreateRequestDto {
    @NotNull
    InterestField category;

    @Min(1) @Max(5)
    int questionCount;

    @NotNull
    InterviewMode interviewMode;
}
