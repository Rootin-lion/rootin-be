package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionProblem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionProblemRepository extends JpaRepository<CompetitionProblem, Long> {

    // 대회 문제 목록 조회 - 해당 대회의 문제를 순서대로 전부 조회 (N+1 방지를 위해 Problem 함께 조회)
    @EntityGraph(attributePaths = "problem")
    List<CompetitionProblem> findByCompetitionOrderByProblemOrderAsc(Competition competition);

    // 문제 상세 조회 - 문제 id + 해당 대회 소속인지까지 함께 검증 (N+1 방지를 위해 Problem 함께 조회)
    @EntityGraph(attributePaths = "problem")
    Optional<CompetitionProblem> findByIdAndCompetition(Long id, Competition competition);
}
