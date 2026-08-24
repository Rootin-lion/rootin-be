package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    //오늘 진행중인 대회 조회
    Optional<Competition> findByCompetitionDate(LocalDate competitionDate);

    //종료된 대회 목록 조회
    Page<Competition> findByEndAtBeforeOrderByCompetitionDateDesc(LocalDateTime now, Pageable pageable);

    // 주간/월간 랭킹 - 해당 기간(주/달)에 열린 대회 전체 조회
    List<Competition> findByCompetitionDateBetween(LocalDate start, LocalDate end);
}
