package com.example.rootin.mypage.dashboard.dto.response;

import com.example.rootin.member.domain.InterestField;

public record BookmarkResponseDto(
        Long problemId,
        InterestField category,
        String title
) {
}
