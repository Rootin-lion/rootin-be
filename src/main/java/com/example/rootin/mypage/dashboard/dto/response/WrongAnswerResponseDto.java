package com.example.rootin.mypage.dashboard.dto.response;

import com.example.rootin.member.domain.InterestField;

public record WrongAnswerResponseDto(
        Long competitionId,
        Long competitionProblemId,
        InterestField category,
        String title
) {
}
