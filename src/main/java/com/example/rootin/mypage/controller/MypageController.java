package com.example.rootin.mypage.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.mypage.bookmark.dto.response.MypageBookmarkResponseDto;
import com.example.rootin.mypage.bookmark.service.MypageBookmarkService;
import com.example.rootin.mypage.dashboard.dto.response.DashboardResponseDto;
import com.example.rootin.mypage.dashboard.service.DashboardService;
import com.example.rootin.mypage.errornote.dto.response.ErrorNoteResponseDto;
import com.example.rootin.mypage.errornote.service.ErrorNoteService;
import com.example.rootin.mypage.report.dto.response.MypageReportResponseDto;
import com.example.rootin.mypage.report.service.MypageReportService;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.mypage.profile.service.ProfileImageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage")
public class MypageController {

    private final DashboardService dashboardService;
    private final MypageReportService mypageReportService;
    private final ErrorNoteService errorNoteService;
    private final MypageBookmarkService mypageBookmarkService;
    private final ProfileImageService profileImageService;

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

    @GetMapping("/errornotes")
    @Operation(summary = "대회 문제 오답 노트 조회")
    public ApiResponse<PageResponse<ErrorNoteResponseDto>> getErrorNotes(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(errorNoteService.getErrorNotes(memberId, page));
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "북마크 문제 조회")
    public ApiResponse<PageResponse<MypageBookmarkResponseDto>> getBookmarks(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(mypageBookmarkService.getBookmarks(memberId, page));
    }

    @PatchMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 사진 변경")
    public ApiResponse<MemberResponseDto> updateProfileImage(
            @AuthenticationPrincipal Long memberId,
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.success(profileImageService.updateProfileImage(memberId, image));
    }
}
