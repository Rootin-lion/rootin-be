package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.*;
import com.example.rootin.interview.dto.response.GeneratedReportResponseDto;
import com.example.rootin.interview.dto.response.InterviewReportItemResponseDto;
import com.example.rootin.interview.dto.response.InterviewReportResponseDto;
import com.example.rootin.interview.repository.InterviewEvaluationRepository;
import com.example.rootin.interview.repository.InterviewReportRepository;
import com.example.rootin.interview.repository.InterviewRepository;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewReportService {

    private final InterviewRepository interviewRepository;
    private final InterviewReportRepository interviewReportRepository;
    private final InterviewEvaluationRepository evaluationRepository;
    private final InterviewAiService interviewAiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public InterviewReport createReport(Long memberId, Long interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INTERVIEW_REPORT_NOT_CREATABLE);
        }

        return interviewReportRepository.findByInterview_Id(interviewId)
                .orElseGet(() -> interviewReportRepository.save(InterviewReport.create(interview)));
    }

    // 실제 Gemini 리포트 생성
    @Async
    @Transactional
    public void generateReportAsync(Long reportId) {
        InterviewReport report = interviewReportRepository.findById(reportId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_REPORT_NOT_FOUND));
        try {
            Long interviewId = report.getInterview().getId();
            List<InterviewEvaluation> evaluations = evaluationRepository.findAllByInterviewId(interviewId);

            if (evaluations.isEmpty()) {
                throw new CustomException(ErrorCode.INTERVIEW_REPORT_GENERATION_FAILED);
            }

            int averageAccuracy = (int) Math.round(evaluations.stream()
                                    .mapToInt(InterviewEvaluation::getAccuracy)
                                    .average()
                                    .orElse(0));

            GeneratedReportResponseDto generated = interviewAiService.generateReport(evaluations);
            report.complete(
                    averageAccuracy,
                    generated.overallFeedback(),
                    serializeFeedbackItems(generated.strengths()),
                    serializeFeedbackItems(generated.weaknesses())
            );

        } catch (Exception e) {
            log.error("Interview report generation failed. reportId={}", reportId, e);
            report.fail();
        }
    }


    @Transactional
    public InterviewReportResponseDto getReport(Long memberId, Long interviewId) {

        InterviewReport report = interviewReportRepository
                        .findByInterview_Id(interviewId)
                        .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_REPORT_NOT_FOUND));

        if (!report.getInterview().getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (report.getStatus() == InterviewReportStatus.FAILED) {
            throw new CustomException(ErrorCode.INTERVIEW_REPORT_GENERATION_FAILED);
        }

        /*
         * 아직 생성 중이면
         * 상세 데이터 조회할 필요 없음
         */
        if (report.getStatus() != InterviewReportStatus.COMPLETED) {
            return InterviewReportResponseDto.generating(report);
        }

        List<InterviewEvaluation> evaluations = evaluationRepository.findAllByInterviewId(interviewId);

        return InterviewReportResponseDto.from(
                report,
                evaluations,
                deserializeFeedbackItems(report.getStrengths()),
                deserializeFeedbackItems(report.getWeaknesses())
        );
    }

    private String serializeFeedbackItems(List<InterviewReportItemResponseDto> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new IllegalStateException("면접 피드백 직렬화에 실패했습니다.", e);
        }
    }

    private List<InterviewReportItemResponseDto> deserializeFeedbackItems(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            InterviewReportItemResponseDto[] items = objectMapper.readValue(
                    json,
                    InterviewReportItemResponseDto[].class
            );
            return Arrays.asList(items);
        } catch (Exception e) {
            throw new IllegalStateException("면접 피드백 역직렬화에 실패했습니다.", e);
        }
    }
}
