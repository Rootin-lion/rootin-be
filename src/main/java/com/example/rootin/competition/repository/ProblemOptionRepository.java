package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Problem;
import com.example.rootin.competition.domain.ProblemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemOptionRepository extends JpaRepository<ProblemOption, Long> {

    // 문제 상세 조회 - 해당 문제의 보기를 순서대로 조회
    List<ProblemOption> findByProblemOrderByOptionOrderAsc(Problem problem);

    // 답안 저장 - 선택한 보기가 해당 문제의 보기인지 검증
    Optional<ProblemOption> findByIdAndProblem(Long id, Problem problem);

    // 마이페이지 - 오답노트. 각 문제의 정답 조회
    @Query("""
            SELECT option
            FROM ProblemOption option
            JOIN FETCH option.problem problem
            WHERE problem.id IN :problemIds
              AND option.isAnswer = true
            """)
    List<ProblemOption> findCorrectOptionsByProblemIds(
            @Param("problemIds") List<Long> problemIds
    );
}
