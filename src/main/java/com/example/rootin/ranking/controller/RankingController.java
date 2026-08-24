package com.example.rootin.ranking.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.ranking.domain.RankingPeriod;
import com.example.rootin.ranking.dto.response.CompetitionRankingItemResponse;
import com.example.rootin.ranking.service.CompetitionRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "랭킹", description = "대회 랭킹 관련 API")
@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class RankingController {

    private final CompetitionRankingService competitionRankingService;

    @Operation(summary = "랭킹 TOP3 조회")
    @GetMapping("/{competitionId}/rankings/top3")
    public ApiResponse<List<CompetitionRankingItemResponse>> getRankingTop3(@PathVariable Long competitionId) {
        return ApiResponse.success(competitionRankingService.getTop3(competitionId));
    }

    @Operation(summary = "랭킹 전체 조회")
    @GetMapping("/{competitionId}/rankings")
    public ApiResponse<PageResponse<CompetitionRankingItemResponse>> getRankings(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "DAILY") RankingPeriod period,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        return ApiResponse.success(competitionRankingService.getRankings(competitionId, period, pageable));
    }

    @Operation(summary = "내 랭킹 조회")
    @GetMapping("/{competitionId}/rankings/me")
    public ApiResponse<CompetitionRankingItemResponse> getMyRanking(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "DAILY") RankingPeriod period,
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(competitionRankingService.getMyRanking(competitionId, period, memberId));
    }
}