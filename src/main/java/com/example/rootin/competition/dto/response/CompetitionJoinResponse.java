package com.example.rootin.competition.dto.response;

import java.time.LocalDateTime;

public record CompetitionJoinResponse (
        Long participantId,
        LocalDateTime startedAt,
        LocalDateTime expiresAt
){}
