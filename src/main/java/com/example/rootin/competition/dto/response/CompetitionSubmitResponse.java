package com.example.rootin.competition.dto.response;

import java.time.LocalDateTime;

public record CompetitionSubmitResponse(
        Long participantId,
        LocalDateTime submittedAt
){}
