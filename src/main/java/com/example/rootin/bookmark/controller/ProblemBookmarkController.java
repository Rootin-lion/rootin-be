package com.example.rootin.bookmark.controller;

import com.example.rootin.bookmark.service.CompetitionProblemBookmarkService;
import com.example.rootin.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "문제 북마크", description = "문제 북마크 관련 API")
@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemBookmarkController {

    private final CompetitionProblemBookmarkService competitionProblemBookmarkService;

    @Operation(summary = "문제 북마크 설정")
    @PostMapping("/{problemId}/bookmark")
    public ApiResponse<Void> addBookmark(
            @PathVariable Long problemId,
            @AuthenticationPrincipal Long memberId
    ) {
        competitionProblemBookmarkService.addBookmark(problemId, memberId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "문제 북마크 해제")
    @DeleteMapping("/{problemId}/bookmark")
    public ApiResponse<Void> removeBookmark(
            @PathVariable Long problemId,
            @AuthenticationPrincipal Long memberId
    ) {
        competitionProblemBookmarkService.removeBookmark(problemId, memberId);
        return ApiResponse.success(null);
    }
}