package com.example.rootin.member.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.member.dto.response.TokenResponseDto;
import com.example.rootin.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    @GetMapping("/kakao")
    @Operation(summary = "카카오 로그인")
    public ResponseEntity<ApiResponse<TokenResponseDto>> kakaoLogin(@RequestParam String code) {
        OAuthService.TokenIssueResult result = oauthService.kakaoLogin(code);
        return buildTokenResponse(result);
    }

    @GetMapping("/google")
    @Operation(summary = "구글 로그인")
    public ResponseEntity<ApiResponse<TokenResponseDto>> googleLogin(@RequestParam String code) {
        OAuthService.TokenIssueResult result = oauthService.googleLogin(code);
        return buildTokenResponse(result);
    }

    @PostMapping("/reissue")
    @Operation(summary = "JWT 재발급")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        OAuthService.TokenIssueResult result = oauthService.reissue(refreshToken);
        return buildTokenResponse(result);
    }

    private ResponseEntity<ApiResponse<TokenResponseDto>> buildTokenResponse(
            OAuthService.TokenIssueResult result
    ) {
        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refreshToken", result.refreshToken())
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .sameSite("Lax")
                        .maxAge(oauthService.getRefreshTokenMaxAgeSeconds())
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(result.response()));
    }
}
