package com.example.rootin.competition.dto.response;

import com.example.rootin.competition.domain.CompetitionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CompetitionTodayResponse(
        Long competitionId,
        LocalDate competitionDate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        CompetitionStatus status,
        long remainingSeconds
) {}
