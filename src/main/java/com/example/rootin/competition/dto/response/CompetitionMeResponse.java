package com.example.rootin.competition.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CompetitionMeResponse(
        Long participantId,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        boolean submitted,
        List<AnsweredProblemResponse> answeredProblems
) {
    public record AnsweredProblemResponse(
            Long problemId,
            Long selectedOptionId
    ) {}
}