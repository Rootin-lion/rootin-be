package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    //오늘 진행중인 대회 조회
    Optional<Competition> findByCompetitionDate(LocalDate competitionDate);

    //종료된 대회 목록 조회
    Page<Competition> findByEndAtBeforeOrderByCompetitionDateDesc(LocalDateTime now, Pageable pageable);
}
