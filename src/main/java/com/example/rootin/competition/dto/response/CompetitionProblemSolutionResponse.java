package com.example.rootin.competition.dto.response;

import com.example.rootin.member.domain.InterestField;

import java.util.List;

public record CompetitionProblemSolutionResponse(
        Long problemId,
        int problemOrder,
        String problemContent,
        InterestField category,
        String problemExplanation,
        List<OptionResponse> options,
        Long selectedOptionId,
        boolean correct
) {
    public record OptionResponse(
            Long optionId,
            String optionContent,
            int optionOrder,
            boolean isAnswer
    ) {}
}