package com.example.rootin.interview.dto.request;

import com.example.rootin.interview.domain.InterviewMode;
import com.example.rootin.member.domain.InterestField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewCreateRequestDto {

    @NotNull(message = "관심 분야를 선택해 주세요.")
    private InterestField field;

    @Min(value = 1, message = "질문 수는 최소 1개 이상이어야 합니다.")
    @Max(value = 5, message = "질문 수는 최대 5개까지 가능합니다.")
    private int questionCount;

    @NotNull(message = "면접 방식을 선택해 주세요.")
    private InterviewMode interviewMode;
}
