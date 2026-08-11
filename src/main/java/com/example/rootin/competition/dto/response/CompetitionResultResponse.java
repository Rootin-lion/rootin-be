package com.example.rootin.competition.dto.response;

import com.example.rootin.competition.domain.ProblemResultStatus;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDate;
import java.util.List;

public record CompetitionResultResponse(
        LocalDate competitionDate,
        int score,
        int totalScore,
        int correctCount,
        int totalCount,
        long solvingTimeSeconds,
        List<ProblemResultResponse> problemResults,
        List<InterestField> strongCategories,
        List<InterestField> weakCategories
) {
    public record ProblemResultResponse(
            int problemOrder,
            Long problemId,
            ProblemResultStatus status,
            int score
    ) {}
}
