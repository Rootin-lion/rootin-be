package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewCreateResponseDto;
import com.example.rootin.interview.repository.InterviewRepository;
import com.example.rootin.interview.repository.InterviewTopicRepository;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewTopicRepository interviewTopicRepository;
    private final MemberRepository memberRepository;

    public InterviewCreateResponseDto createInterview(Long memberId, InterviewCreateRequestDto request){
        Member member = memberRepository.findById(memberId).orElseThrow
                (()-> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<InterviewTopic> selectedTopics = interviewTopicRepository.findRandomTopicsByField(
                request.getCategory().name(),
                request.getQuestionCount());
        if (selectedTopics.size() < request.getQuestionCount()) {
            throw new IllegalArgumentException("선택 가능한 면접 토픽이 부족합니다.");
        }

        List<Long> selectedTopicIds = selectedTopics.stream().map(InterviewTopic::getId).toList();
        Interview interview = Interview.create(member, request.getCategory(), request.getQuestionCount(), selectedTopicIds);
        interviewRepository.save(interview);

        return InterviewCreateResponseDto.from(interview);
    }



}
