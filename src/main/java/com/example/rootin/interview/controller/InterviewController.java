package com.example.rootin.interview.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewCreateResponseDto;
import com.example.rootin.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interviews/sessions")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewCreateResponseDto>> createInterview(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody InterviewCreateRequestDto request
    ) {
        InterviewCreateResponseDto response = interviewService.createInterview(memberId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
