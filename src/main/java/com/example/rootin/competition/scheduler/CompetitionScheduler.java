package com.example.rootin.competition.scheduler;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.repository.CompetitionRepository;
import com.example.rootin.competition.service.CompetitionCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CompetitionScheduler {

    private final CompetitionCreateService competitionCreateService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")  // 매일 자정
    public void createDailyCompetition() {
        competitionCreateService.createDailyCompetition(LocalDate.now());
    }
}