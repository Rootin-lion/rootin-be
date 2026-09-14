package com.example.rootin.interview.controller;

import com.example.rootin.global.common.ApiResponse;
import com.example.rootin.interview.dto.request.InterviewAnswerRequestDto;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewAnswerSubmitResponseDto;
import com.example.rootin.interview.dto.response.InterviewCreateResponseDto;
import com.example.rootin.interview.dto.response.InterviewQuestionResponseDto;
import com.example.rootin.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/sessions")
    @Operation(summary = "면접 세션 생성", description = "세션 생성과 동시에 첫 질문 반환")
    public ResponseEntity<ApiResponse<InterviewCreateResponseDto>> createInterview(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody InterviewCreateRequestDto request
    ) {
        InterviewCreateResponseDto response = interviewService.createInterview(memberId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/{interviewId}/answers")
    @Operation(summary = "답변 제출 및 다음 질문 반환", description = "답변을 분석하여 꼬리/기본 질문 반환")
    public ResponseEntity<ApiResponse<InterviewAnswerSubmitResponseDto>> submitAnswer(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewAnswerRequestDto request
    ) {
        InterviewAnswerSubmitResponseDto response = interviewService.submitAnswer(memberId, interviewId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{interviewId}/questions/current")
    @Operation(summary = "현재 질문 조회", description = "테스트 용")
    public ResponseEntity<ApiResponse<InterviewQuestionResponseDto>> getCurrentQuestion(
            @PathVariable Long interviewId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(interviewService.getCurrentQuestion(interviewId)));
    }
}
