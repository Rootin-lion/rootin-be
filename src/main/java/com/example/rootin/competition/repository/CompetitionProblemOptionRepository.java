package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionProblemOptionRepository extends JpaRepository<CompetitionProblemOption, Long> {

    // 문제 상세 조회 - 해당 문제에 속한 보기를 순서대로 전부 조회
    List<CompetitionProblemOption> findByCompetitionProblemOrderByOptionOrderAsc(CompetitionProblem competitionProblem);

    // 답안 저장 - 선택한 보기가 실제로 해당 문제에 속한 보기인지 검증
    Optional<CompetitionProblemOption> findByIdAndCompetitionProblem(Long id, CompetitionProblem competitionProblem);
}
