package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.*;
import com.example.rootin.competition.dto.request.CompetitionAnswerRequest;
import com.example.rootin.competition.dto.response.*;
import com.example.rootin.competition.exception.*;
import com.example.rootin.competition.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private static final long PROBLEM_TIME_LIMIT_MINUTES = 30; // 문제 풀이 시간 30분 제한
    private static final int PROBLEM_COUNT = 10; // 대회 문제 10개

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionProblemRepository competitionProblemRepository;
    private final CompetitionProblemOptionRepository competitionProblemOptionRepository;
    private final CompetitionProblemSubmissionRepository competitionProblemSubmissionRepository;

    @Transactional(readOnly = true)
    public CompetitionTodayResponse getTodayCompetition() {
        LocalDate today = LocalDate.now();
        Competition competition = competitionRepository.findByCompetitionDate(today)
                .orElseThrow(CompetitionNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        CompetitionStatus status = competition.getStatus(now);
        long remainingSeconds = switch (status) {
            case BEFORE_START -> Duration.between(now, competition.getStartAt()).getSeconds();
            case IN_PROGRESS -> Duration.between(now, competition.getEndAt()).getSeconds();
            case CLOSED -> 0;
        };

        return new CompetitionTodayResponse(
                competition.getId(),
                competition.getCompetitionDate(),
                competition.getStartAt(),
                competition.getEndAt(),
                status,
                remainingSeconds
        );
    }

    @Transactional
    public CompetitionJoinResponse join(Long competitionId, Long memberId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        if (competition.getStatus(now) != CompetitionStatus.IN_PROGRESS) {
            throw new CompetitionNotJoinableException();
        }

        return competitionParticipantRepository.findByMemberIdAndCompetition(memberId, competition)
                .map(existing -> reenter(existing, competition, now))
                .orElseGet(() -> newJoin(memberId, competition, now));
    }

    // 기존 참여 기록이 있는 경우: 제출 전 + 마감 전이면 재입장 허용 아니면 재참여 불가(제한 시간 지나면 재참여 불가)
    private CompetitionJoinResponse reenter(CompetitionParticipant participant, Competition competition, LocalDateTime now) {
        LocalDateTime expiresAt = calculateExpiresAt(participant.getStartedAt(), competition);

        if (participant.getSubmittedAt() != null || now.isAfter(expiresAt)) {
            throw new CompetitionAlreadyJoinedException();
        }

        return new CompetitionJoinResponse(participant.getId(), participant.getStartedAt(), expiresAt);
    }

    private CompetitionJoinResponse newJoin(Long memberId, Competition competition, LocalDateTime now) {
        CompetitionParticipant participant = new CompetitionParticipant(memberId, competition, now);
        competitionParticipantRepository.save(participant);

        LocalDateTime expiresAt = calculateExpiresAt(now, competition);
        return new CompetitionJoinResponse(participant.getId(), participant.getStartedAt(), expiresAt);
    }

    // 참여시각 + 30분과 대회 종료시각 중 더 빠른 시각을 마감시각으로 계산
    private LocalDateTime calculateExpiresAt(LocalDateTime startedAt, Competition competition) {
        LocalDateTime expiresAt = startedAt.plusMinutes(PROBLEM_TIME_LIMIT_MINUTES);
        return expiresAt.isAfter(competition.getEndAt()) ? competition.getEndAt() : expiresAt;
    }

    @Transactional(readOnly = true)
    public Page<CompetitionClosedResponse> getClosedCompetitions(Long memberId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Competition> competitions = competitionRepository.findByEndAtBeforeOrderByCompetitionDateDesc(now, pageable);

        return competitions.map(competition -> {
            long participantCount = competitionParticipantRepository.countByCompetition(competition);
            boolean viewable = competitionParticipantRepository.existsByMemberIdAndCompetition(memberId, competition);

            return new CompetitionClosedResponse(
                    competition.getId(),
                    competition.getCompetitionDate(),
                    PROBLEM_COUNT,
                    (int) PROBLEM_TIME_LIMIT_MINUTES,
                    participantCount,
                    viewable
            );
        });
    }

    @Transactional(readOnly = true)
    public CompetitionProblemListResponse getProblems(Long competitionId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        List<CompetitionProblem> problems = competitionProblemRepository.findByCompetitionOrderByProblemOrderAsc(competition);

        List<CompetitionProblemSummaryResponse> summaries = problems.stream()
                .map(problem -> new CompetitionProblemSummaryResponse(problem.getId(), problem.getProblemOrder()))
                .toList();

        // 처음 진입 시 1번 문제(순서상 첫 번째)를 바로 보여줄 수 있도록 상세까지 같이 반환
        CompetitionProblemDetailResponse firstProblem = problems.isEmpty()
                ? null
                : buildProblemDetailResponse(problems.get(0));

        return new CompetitionProblemListResponse(summaries, firstProblem);
    }

    @Transactional(readOnly = true)
    public CompetitionProblemDetailResponse getProblemDetail(Long competitionId, Long problemId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        CompetitionProblem problem = competitionProblemRepository.findByIdAndCompetition(problemId, competition)
                .orElseThrow(CompetitionProblemNotFoundException::new);

        return buildProblemDetailResponse(problem);
    }

    private CompetitionProblemDetailResponse buildProblemDetailResponse(CompetitionProblem problem) {
        List<CompetitionProblemDetailResponse.OptionResponse> options =
                competitionProblemOptionRepository.findByCompetitionProblemOrderByOptionOrderAsc(problem).stream()
                        .map(option -> new CompetitionProblemDetailResponse.OptionResponse(
                                option.getId(), option.getOptionContent(), option.getOptionOrder()))
                        .toList();

        return new CompetitionProblemDetailResponse(
                problem.getId(),
                problem.getProblemOrder(),
                problem.getProblemContent(),
                problem.getCategory(),
                options
        );
    }

    @Transactional(readOnly = true)
    public CompetitionMeResponse getMe(Long competitionId, Long memberId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        CompetitionParticipant participant = competitionParticipantRepository
                .findByMemberIdAndCompetition(memberId, competition)
                .orElseThrow(CompetitionParticipantNotFoundException::new);

        LocalDateTime expiresAt = calculateExpiresAt(participant.getStartedAt(), competition);

        List<CompetitionMeResponse.AnsweredProblemResponse> answeredProblems =
                competitionProblemSubmissionRepository.findByCompetitionParticipant(participant).stream()
                        .map(submission -> new CompetitionMeResponse.AnsweredProblemResponse(
                                submission.getCompetitionProblem().getId(),
                                submission.getSelectedOption().getId()
                        ))
                        .toList();

        return new CompetitionMeResponse(
                participant.getId(),
                participant.getStartedAt(),
                expiresAt,
                participant.getSubmittedAt() != null,
                answeredProblems
        );
    }

    @Transactional
    public void saveAnswer(Long competitionId, Long memberId, CompetitionAnswerRequest request) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        CompetitionParticipant participant = competitionParticipantRepository
                .findByMemberIdAndCompetition(memberId, competition)
                .orElseThrow(CompetitionParticipantNotFoundException::new);

        if (participant.getSubmittedAt() != null) {
            throw new CompetitionAlreadySubmittedException();
        }

        LocalDateTime expiresAt = calculateExpiresAt(participant.getStartedAt(), competition);
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new CompetitionTimeExpiredException();
        }

        CompetitionProblem problem = competitionProblemRepository.findByIdAndCompetition(request.problemId(), competition)
                .orElseThrow(CompetitionProblemNotFoundException::new);

        CompetitionProblemOption option = competitionProblemOptionRepository
                .findByIdAndCompetitionProblem(request.selectedOptionId(), problem)
                .orElseThrow(CompetitionOptionNotFoundException::new);

        competitionProblemSubmissionRepository.findByCompetitionParticipantAndCompetitionProblem(participant, problem)
                .ifPresentOrElse(
                        submission -> submission.changeAnswer(option, option.isAnswer()),
                        () -> competitionProblemSubmissionRepository.save(
                                new CompetitionProblemSubmission(option, participant, problem, option.isAnswer()))
                );
    }

    @Transactional
    public CompetitionSubmitResponse submit(Long competitionId, Long memberId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        CompetitionParticipant participant = competitionParticipantRepository
                .findByMemberIdAndCompetition(memberId, competition)
                .orElseThrow(CompetitionParticipantNotFoundException::new);

        if (participant.getSubmittedAt() != null) {
            throw new CompetitionAlreadySubmittedException();
        }
        participant.submit(LocalDateTime.now());

        return new CompetitionSubmitResponse(participant.getId(), participant.getSubmittedAt());
    }
}