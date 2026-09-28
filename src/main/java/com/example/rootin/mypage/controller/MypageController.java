package com.example.rootin.mypage.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.mypage.dashboard.dto.response.DashboardResponseDto;
import com.example.rootin.mypage.dashboard.service.DashboardService;
import com.example.rootin.mypage.report.dto.response.MypageReportResponseDto;
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
public class MypageController {

    private final DashboardService dashboardService;
    private final MypageReportService mypageReportService;

    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 조회")
    public ApiResponse<DashboardResponseDto> getDashboard(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(dashboardService.getDashboard(memberId));
    }

    @GetMapping("/reports")
    @Operation(summary = "마이페이지 면접 리포트 조회")
    public ApiResponse<PageResponse<MypageReportResponseDto>> getReports(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(mypageReportService.getReports(memberId, page));
    }
}