package com.example.rootin.competition.controller;

import com.example.rootin.competition.dto.response.CompetitionJoinResponse;
import com.example.rootin.competition.dto.response.CompetitionTodayResponse;
import com.example.rootin.competition.service.CompetitionService;
import com.example.rootin.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @Operation(summary = "현재 진행중인 대회 조회")
    @GetMapping("/today")
    public ApiResponse<CompetitionTodayResponse> getTodayCompetition() {
        return ApiResponse.success(competitionService.getTodayCompetition());
    }

    @Operation(summary = "대회 참여(입장)")
    @PostMapping("/{competitionId}/join")
    public ApiResponse<CompetitionJoinResponse> join(
            @PathVariable Long competitionId,
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(competitionService.join(competitionId, memberId));
    }
}