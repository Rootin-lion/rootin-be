package com.example.rootin.mypage.dashboard.dto.response;

import com.example.rootin.member.domain.InterestField;

public record CategoryAccuracyResponseDto(
        InterestField category,
        double accuracyRate
) {
}
