package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewSessionResponseDto;
import com.example.rootin.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;

    @Transactional
    public InterviewSessionResponseDto createSession(Long memberId, InterviewCreateRequestDto request) {
        Interview interview = Interview.create(
                memberId,
                request.getField(),
                request.getQuestionCount(),
                request.getInterviewMode()
        );

        Interview savedInterview = interviewRepository.save(interview);
        return new InterviewSessionResponseDto(
                savedInterview.getId(),
                savedInterview.getField(),
                savedInterview.getQuestionCount(),
                savedInterview.getInterviewMode(),
                savedInterview.getStatus(),
                savedInterview.getStartedAt()
        );
    }
}
