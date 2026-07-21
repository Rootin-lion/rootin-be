package com.example.rootin.member.controller;

import com.example.rootin.member.dto.request.ProfileRequestDto;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.member.dto.response.NicknameCheckResponseDto;
import com.example.rootin.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PutMapping("/profile")
    @Operation(summary = "사용자 프로필 설정", description = "닉네임, 연령, 관심분야")
    public ResponseEntity<MemberResponseDto>
    completeProfile(Authentication authentication,
                    @Valid @RequestBody ProfileRequestDto request) {
        Long memberId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(memberService.completeProfile(memberId, request));
    }


    @GetMapping("/profile")
    @Operation(summary = "사용자 정보 확인")
    public ResponseEntity<MemberResponseDto> getprofile(Authentication authentication) {
        Long memberId =
                (Long) authentication.getPrincipal();

        return ResponseEntity.ok(memberService.getProfile(memberId));
    }

    @GetMapping("/nickname/check")
    @Operation(summary = "닉네임 중복 확인")
    public ResponseEntity<NicknameCheckResponseDto> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(memberService.checkNickname(nickname));
    }


}