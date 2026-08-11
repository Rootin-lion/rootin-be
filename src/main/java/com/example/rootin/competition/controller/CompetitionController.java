package com.example.rootin.competition.controller;

import com.example.rootin.competition.dto.request.CompetitionAnswerRequest;
import com.example.rootin.competition.dto.response.*;
import com.example.rootin.competition.service.CompetitionService;
import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "종료된 대회 목록 조회")
    @GetMapping
    public ApiResponse<PageResponse<CompetitionClosedResponse>> getClosedCompetitions(
            @RequestParam(defaultValue = "CLOSED") String status,
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal Long memberId
    ) {
        Pageable pageable = PageRequest.of(page, 4, Sort.by(Sort.Direction.DESC, "competitionDate"));
        Page<CompetitionClosedResponse> result = competitionService.getClosedCompetitions(memberId, pageable);
        return ApiResponse.success(PageResponse.of(result));
    }

    @Operation(summary = "대회 문제 목록 조회")
    @GetMapping("/{competitionId}/problems")
    public ApiResponse<CompetitionProblemListResponse> getProblems(@PathVariable Long competitionId) {
        return ApiResponse.success(competitionService.getProblems(competitionId));
    }

    @Operation(summary = "문제 상세 조회")
    @GetMapping("/{competitionId}/problems/{problemId}")
    public ApiResponse<CompetitionProblemDetailResponse> getProblemDetail(
            @PathVariable Long competitionId,
            @PathVariable Long problemId
    ) {
        return ApiResponse.success(competitionService.getProblemDetail(competitionId, problemId));
    }

    @Operation(summary = "대회 진행 정보 조회")
    @GetMapping("/{competitionId}/me")
    public ApiResponse<CompetitionMeResponse> getMe(
            @PathVariable Long competitionId,
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(competitionService.getMe(competitionId, memberId));
    }

    @Operation(summary = "문제별 답안 저장")
    @PatchMapping("/{competitionId}/answers")
    public ApiResponse<Void> saveAnswer(
            @PathVariable Long competitionId,
            @RequestBody @Valid CompetitionAnswerRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        competitionService.saveAnswer(competitionId, memberId, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "대회 최종 제출")
    @PostMapping("/{competitionId}/submit")
    public ApiResponse<CompetitionSubmitResponse> submit(
            @PathVariable Long competitionId,
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(competitionService.submit(competitionId, memberId));
    }
}