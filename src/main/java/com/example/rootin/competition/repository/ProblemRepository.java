package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // 최근 출제된 문제를 제외하고 분야별 랜덤 조회
    @Query(value = """
        SELECT p.* FROM problem p
        WHERE p.category = :category
          AND p.id NOT IN (
              SELECT cp.problem_id FROM competition_problem cp
              JOIN competition c ON cp.competition_id = c.id
              WHERE c.competition_date >= :since
          )
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Problem> findRandomExcludingRecent(@Param("category") String category,
                                            @Param("since") LocalDate since,
                                            @Param("limit") int limit);
    // 부족할 때 보충용 - 분야 내 전체에서 랜덤 조회
    @Query(value = """
        SELECT p.* FROM problem p
        WHERE p.category = :category
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<Problem> findRandomByCategory(@Param("category") String category,
                                       @Param("limit") int limit);

}
