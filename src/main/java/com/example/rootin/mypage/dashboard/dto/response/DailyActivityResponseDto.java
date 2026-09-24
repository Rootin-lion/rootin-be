package com.example.rootin.mypage.dashboard.dto.response;

import java.time.LocalDate;

// 잔디
public record DailyActivityResponseDto(
        LocalDate date,
        long competitionCount,
        long interviewCount,
        long totalCount
) {
    public static DailyActivityResponseDto of(
            LocalDate date,
            long competitionCount,
            long interviewCount
    ) {
        return new DailyActivityResponseDto(
                date,
                competitionCount,
                interviewCount,
                competitionCount + interviewCount
        );
    }
}
