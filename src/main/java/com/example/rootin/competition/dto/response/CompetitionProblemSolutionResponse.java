package com.example.rootin.competition.dto.response;

import com.example.rootin.member.domain.InterestField;

import java.util.List;

public record CompetitionProblemSolutionResponse(
        Long competitionProblemId,       // 대회 문제 id (답안 제출, 해설 조회에 사용)
        Long problemId,      // 문제 은행 id
        int problemOrder,
        String problemTitle,
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