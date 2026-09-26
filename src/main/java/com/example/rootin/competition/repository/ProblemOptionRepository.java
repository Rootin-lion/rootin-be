package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Problem;
import com.example.rootin.competition.domain.ProblemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemOptionRepository extends JpaRepository<ProblemOption, Long> {

    // 문제 상세 조회 - 해당 문제의 보기를 순서대로 조회
    List<ProblemOption> findByProblemOrderByOptionOrderAsc(Problem problem);

    // 답안 저장 - 선택한 보기가 해당 문제의 보기인지 검증
    Optional<ProblemOption> findByIdAndProblem(Long id, Problem problem);
}
