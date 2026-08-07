package com.example.rootin.competition.dto.response;

import com.example.rootin.member.domain.InterestField;

import java.util.List;

public record CompetitionProblemDetailResponse(
        Long problemId,
        Integer problemOrder,
        String problemContent,
        InterestField category,
        List<OptionResponse> options
) {
    public record OptionResponse(
            Long optionId,
            String optionContent,
            Integer optionOrder
    ) {}
}