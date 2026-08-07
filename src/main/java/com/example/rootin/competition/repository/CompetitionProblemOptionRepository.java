package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetitionProblemOptionRepository extends JpaRepository<CompetitionProblemOption, Long> {

    // 문제 상세 조회 - 해당 문제에 속한 보기를 순서대로 전부 조회
    List<CompetitionProblemOption> findByCompetitionProblemOrderByOptionOrderAsc(CompetitionProblem competitionProblem);
}
