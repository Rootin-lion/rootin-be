package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.domain.CompetitionStatus;
import com.example.rootin.competition.dto.response.CompetitionJoinResponse;
import com.example.rootin.competition.dto.response.CompetitionTodayResponse;
import com.example.rootin.competition.exception.CompetitionAlreadyJoinedException;
import com.example.rootin.competition.exception.CompetitionNotFoundException;
import com.example.rootin.competition.exception.CompetitionNotJoinableException;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private static final long PROBLEM_TIME_LIMIT_MINUTES = 30; // 문제 풀이 시간 30분 제한

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;

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
}