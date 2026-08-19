package com.example.rootin.ranking.dto.response;

import java.time.LocalDateTime;

public record CompetitionRankingItemResponse(
        int rank,
        Long memberId,
        String nickname,
        String imgUrl,
        int score,
        LocalDateTime submittedAt
) {}