package com.example.rootin.interview.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewSessionResponseDto;
import com.example.rootin.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/sessions")
    @Operation(summary = "면접 세션 생성", description = "관심 분야, 질문 수, 면접 방식을 받아 세션 생성")
    public ResponseEntity<ApiResponse<InterviewSessionResponseDto>> createSession(
            Authentication authentication,
            @Valid @RequestBody InterviewCreateRequestDto request
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(interviewService.createSession(memberId, request)));
    }

}
