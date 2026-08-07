package com.example.rootin.competition.dto.response;

import java.util.List;

public record CompetitionProblemListResponse(
        List<CompetitionProblemSummaryResponse> problems, // 사이드바 1~10번용
        CompetitionProblemDetailResponse firstProblem      // 대회 입장 시 보여줄 1번 문제
) {}