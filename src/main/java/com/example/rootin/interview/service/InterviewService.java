package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.*;
import com.example.rootin.interview.dto.request.InterviewAnswerRequestDto;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.*;
import com.example.rootin.interview.exception.InterviewMemberNotFoundException;
import com.example.rootin.interview.exception.InterviewTopicInsufficientException;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.repository.*;
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
    private final InterviewAiService interviewAiService;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewEvaluationRepository interviewEvaluationRepository;

    public InterviewCreateResponseDto createInterview(Long memberId, InterviewCreateRequestDto request){
        Member member = memberRepository.findById(memberId).orElseThrow
                (InterviewMemberNotFoundException::new);

        List<InterviewTopic> selectedTopics = interviewTopicRepository.findRandomTopicsByField(
                request.getCategory().name(),
                request.getQuestionCount());
        if (selectedTopics.size() < request.getQuestionCount()) {
            throw new InterviewTopicInsufficientException();
        }

        List<Long> selectedTopicIds = selectedTopics.stream().map(InterviewTopic::getId).toList();
        Interview interview = Interview.create(
                member,
                request.getCategory(),
                request.getInterviewMode(),
                request.getQuestionCount(),
                selectedTopicIds
        );
        interviewRepository.save(interview);

        //첫 topic
        InterviewTopic firstTopic = selectedTopics.get(0);
        //첫 질문 생성
        GeneratedQuestionResponseDto generated = interviewAiService.generateBasicQuestion(firstTopic);
        InterviewQuestion firstQuestion = InterviewQuestion.create(
                interview, firstTopic, QuestionType.BASIC, generated.question(),
                generated.targetKeywords(), generated.evaluationCriteria(), 1);
        interviewQuestionRepository.save(firstQuestion);


        return InterviewCreateResponseDto.from(interview, firstQuestion);
    }

    public InterviewAnswerSubmitResponseDto submitAnswer(Long memberId, Long interviewId, InterviewAnswerRequestDto request){
        Interview interview = interviewRepository.findById(interviewId).orElseThrow(
                () -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));
        if(!interview.getMember().getId().equals(memberId)){
            throw new CustomException(ErrorCode.FORBIDDEN); //본인면접인지 체크
        }

        InterviewQuestion question = interviewQuestionRepository.findById(request.getQuestionId()).orElseThrow(
                () -> new CustomException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
        if(!question.getInterview().getId().equals(interviewId)){
            throw new CustomException(ErrorCode.INTERVIEW_QUESTION_MISMATCH);
        }
        if (interviewAnswerRepository.existsByQuestion_Id(question.getId())) {
            throw new CustomException(ErrorCode.INTERVIEW_QUESTION_ALREADY_ANSWERED);
        }

        //사용자 답변 저장
        InterviewAnswer answer = InterviewAnswer.create(question, request.getAnswer());
        interviewAnswerRepository.save(answer);

        //AI 답변 평가
        AnswerEvaluationResponseDto aiEvaluation = interviewAiService.evaluateAnswer(question, request.getAnswer());
        InterviewEvaluation evaluation = InterviewEvaluation.create(
                answer, aiEvaluation.level(), aiEvaluation.accuracy(), aiEvaluation.feedback(),
                aiEvaluation.matchedKeywords(), aiEvaluation.missingKeywords()
        );
        interviewEvaluationRepository.save(evaluation);

        //AnswerLevel이 EMBIGUOUS일 경우 꼬리질문 여부
        if(aiEvaluation.level() == AnswerLevel.AMBIGUOUS){
            boolean followUpExist = interviewQuestionRepository.existsByInterview_IdAndTopic_IdAndQuestionType(
                    interviewId, question.getTopic().getId(), QuestionType.FOLLOW_UP);

            if(!followUpExist){
                InterviewQuestion followUp = createFollowUpQuestion(interview, question, request.getAnswer(), aiEvaluation);
                return new InterviewAnswerSubmitResponseDto(
                        interviewId,
                        false,
                        false,
                        aiEvaluation.level(),
                        aiEvaluation.accuracy(),
                        aiEvaluation.feedback(),
                        InterviewQuestionResponseDto.from(followUp));
            }
        }
        // GOOD, UNKNOWN, 꼬리질문 마친 AMBIGUOUS -> 현재 topic 종료, 마지막 Topic이라면 면접 종료
        if(interview.isLastTopic()){
            interview.complete();

            return new InterviewAnswerSubmitResponseDto(
                    interviewId,
                    true,
                    true,
                    aiEvaluation.level(),
                    aiEvaluation.accuracy(),
                    aiEvaluation.feedback(),
                    null
            );
        }
        //다음 topic 이동
        interview.moveToNextTopic();
        InterviewQuestion nextQuestion = createNextBasicQuestion(interview);

        return new InterviewAnswerSubmitResponseDto(
                interviewId,
                true,
                false,
                aiEvaluation.level(),
                aiEvaluation.accuracy(),
                aiEvaluation.feedback(),
                InterviewQuestionResponseDto.from(nextQuestion)
        );

    }

    private InterviewQuestion createNextBasicQuestion(Interview interview){
        Long topicId = interview.getCurrentTopicId();
        InterviewTopic topic = interviewTopicRepository.findById(topicId).orElseThrow(
                () -> new CustomException(ErrorCode.INTERVIEW_INVALID_STATE));

        GeneratedQuestionResponseDto generated = interviewAiService.generateBasicQuestion(topic);
        int questionOrder = (int)interviewQuestionRepository.countByInterview_Id(interview.getId())+1;
        InterviewQuestion question = InterviewQuestion.create(
                interview, topic, QuestionType.BASIC, generated.question(),
                generated.targetKeywords(), generated.evaluationCriteria(), questionOrder
        );

        return interviewQuestionRepository.save(question);
    }

    private InterviewQuestion createFollowUpQuestion(
            Interview interview,
            InterviewQuestion previousQuestion,
            String answer,
            AnswerEvaluationResponseDto evaluation
    ) {

        GeneratedQuestionResponseDto generated = interviewAiService.generateFollowUpQuestion(
                        previousQuestion, answer, evaluation);

        int questionOrder = (int) interviewQuestionRepository.countByInterview_Id(interview.getId()) + 1;

        InterviewQuestion followUp =
                InterviewQuestion.create(
                        interview,
                        previousQuestion.getTopic(),
                        QuestionType.FOLLOW_UP,
                        generated.question(),
                        generated.targetKeywords(),
                        generated.evaluationCriteria(),
                        questionOrder
                );

        return interviewQuestionRepository.save(followUp);
    }

    @Transactional 
    public InterviewQuestionResponseDto getCurrentQuestion(Long memberId, Long interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_NOT_FOUND));
        if (!interview.getMember().getId().equals(memberId)){
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.INTERVIEW_INVALID_STATE);
        }

        InterviewQuestion currentQuestion = interviewQuestionRepository
                .findFirstByInterview_IdAndAnswerIsNullOrderByQuestionOrderDesc(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.INTERVIEW_INVALID_STATE));

        return InterviewQuestionResponseDto.from(currentQuestion);
    }

}
