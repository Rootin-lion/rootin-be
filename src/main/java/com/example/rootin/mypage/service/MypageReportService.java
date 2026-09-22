package com.example.rootin.mypage.service;

import com.example.rootin.global.common.PageResponse;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewReport;
import com.example.rootin.interview.domain.InterviewReportStatus;
import com.example.rootin.interview.dto.response.MypageReportResponseDto;
import com.example.rootin.interview.repository.InterviewQuestionRepository;
import com.example.rootin.interview.repository.InterviewReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageReportService {

    private static final int REPORT_PAGE_SIZE = 4;

    private final InterviewReportRepository interviewReportRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    public PageResponse<MypageReportResponseDto> getReports(Long memberId, int page) {
        if (page < 1) { throw new CustomException(ErrorCode.MYPAGE_REPORT_PAGE_NOT_FOUND);}
        //1페이지 부터 최근 생성 완료된 리포트 순서대로 4개씩 반환
        PageRequest pageable = PageRequest.of(
                page - 1,
                REPORT_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "completedAt")
        );

        Page<InterviewReport> reports = interviewReportRepository
                .findByInterviewId_MemberIdAndStatus(
                        memberId,
                        InterviewReportStatus.COMPLETED,
                        pageable
                );

        if (reports.getTotalElements() == 0) {
            throw new CustomException(ErrorCode.MYPAGE_REPORT_LIST_EMPTY);
        }

        if (reports.getContent().isEmpty()) {
            throw new CustomException(ErrorCode.MYPAGE_REPORT_PAGE_NOT_FOUND);
        }

        List<Long> interviewIds = reports.getContent().stream()
                .map(report -> report.getInterviewId().getId())
                .toList();
        //BASIC 질문 한번에 조회
        Map<Long, List<InterviewQuestion>> basicQuestionsByInterview = interviewIds.isEmpty()
                ? Map.of()
                : interviewQuestionRepository
                        .findBasicQuestionsByInterviewIds(interviewIds)
                        .stream()
                        //면접 id별로 질문 그룹화
                        .collect(Collectors.groupingBy(question -> question.getInterviewId().getId()));

        Page<MypageReportResponseDto> responsePage = reports.map(report -> {
            List<InterviewQuestion> basicQuestions = basicQuestionsByInterview //현재 질문의 BASIC 질문 찾기
                    .getOrDefault(report.getInterviewId().getId(), List.of());

            List<String> topicNames = basicQuestions.stream() //질문의 topicName만 추출
                    .map(question -> question.getTopicId().getTopicName())
                    .toList();

            return MypageReportResponseDto.from(
                    report,
                    topicNames
            );
        });

        return PageResponse.of(responsePage);
    }
}
