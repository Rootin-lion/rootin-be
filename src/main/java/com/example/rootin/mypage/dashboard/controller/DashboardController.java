package com.example.rootin.mypage.dashboard.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.mypage.dashboard.dto.response.DashboardResponseDto;
import com.example.rootin.mypage.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "대시보드 조회")
    public ApiResponse<DashboardResponseDto> getDashboard(
            @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.success(dashboardService.getDashboard(memberId));
    }
}
