package com.example.rootin.competition.scheduler;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.repository.CompetitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CompetitionScheduler {

    private final CompetitionRepository competitionRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void createDailyCompetition() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atTime(12, 0);
        LocalDateTime end = today.atTime(13, 0);

        competitionRepository.save(new Competition(today, start, end));
    }
}