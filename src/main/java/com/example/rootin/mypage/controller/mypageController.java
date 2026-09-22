package com.example.rootin.mypage.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.interview.dto.response.MypageReportResponseDto;
import com.example.rootin.mypage.service.MypageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class mypageController {
    private final MypageReportService mypageService;

    @GetMapping("/reports")
    public ApiResponse<PageResponse<MypageReportResponseDto>> getReports(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(mypageService.getReports(memberId, page));
    }
}
