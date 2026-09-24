package com.example.rootin.mypage.report.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.mypage.report.dto.MypageReportResponseDto;
import com.example.rootin.mypage.report.service.MypageReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MypageReportController {
    private final MypageReportService mypageReportService;

    @GetMapping("/reports")
    @Operation(summary = "마이페이지 면접 리포트 조회", description = "닉네임, 연령대, 관심분야 설정")
    public ApiResponse<PageResponse<MypageReportResponseDto>> getReports(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(mypageReportService.getReports(memberId, page));
    }
}
