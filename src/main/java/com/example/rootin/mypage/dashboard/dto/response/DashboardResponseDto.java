package com.example.rootin.mypage.dashboard.dto.response;

import java.util.List;

public record DashboardResponseDto(
        long totalSolvedCounts,
        double accuracyRate,
        long points,
        int streakDays,
        List<DailyActivityResponseDto> dailyActivities,
        List<CategoryAccuracyResponseDto> categoryAccuracies
) {
}
