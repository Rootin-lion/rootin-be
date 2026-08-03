package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionStatus;
import com.example.rootin.competition.dto.response.CompetitionTodayResponse;
import com.example.rootin.competition.exception.CompetitionNotFoundException;
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

    private final CompetitionRepository competitionRepository;

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
}