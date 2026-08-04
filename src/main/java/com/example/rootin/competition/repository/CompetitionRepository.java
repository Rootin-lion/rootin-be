package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    //오늘 진행중인 대회 조회
    Optional<Competition> findByCompetitionDate(LocalDate competitionDate);
}
