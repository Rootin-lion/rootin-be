package com.example.rootin.competition.dto.request;

import jakarta.validation.constraints.NotNull;

public record CompetitionAnswerRequest(
        @NotNull Long competitionProblemId,
        @NotNull Long selectedOptionId
) {}
