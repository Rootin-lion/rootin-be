package com.example.rootin.member.controller;

import com.example.rootin.member.dto.request.RefreshTokenRequestDto;
import com.example.rootin.member.dto.response.TokenResponseDto;
import com.example.rootin.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    @GetMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인")
    public ResponseEntity<TokenResponseDto> kakaoLogin(@RequestParam String code) {
        return ResponseEntity.ok(oauthService.kakaoLogin(code));
    }

    @PostMapping("/reissue")
    @Operation(summary = "JWT 재발급")
    public ResponseEntity<TokenResponseDto> reissue(
            @RequestBody RefreshTokenRequestDto request
    ) {
        return ResponseEntity.ok(oauthService.reissue(request));
    }
}
