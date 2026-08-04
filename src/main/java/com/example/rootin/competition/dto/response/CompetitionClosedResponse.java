package com.example.rootin.competition.dto.response;

import java.time.LocalDate;

public record CompetitionClosedResponse(
        Long competitionId,
        LocalDate competitionDate,
        int problemCount,
        int timeLimitMinutes,
        long participantCount,
        boolean viewable // false면 결과 보기 버튼 비활성화
) {}

