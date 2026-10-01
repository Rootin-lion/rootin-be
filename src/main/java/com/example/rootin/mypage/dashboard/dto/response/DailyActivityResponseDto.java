package com.example.rootin.mypage.dashboard.dto.response;

import java.time.LocalDate;

// 잔디
public record DailyActivityResponseDto(
        LocalDate date,
        long totalCount
) {
}
