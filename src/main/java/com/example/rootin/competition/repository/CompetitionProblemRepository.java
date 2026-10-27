package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionProblem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompetitionProblemRepository extends JpaRepository<CompetitionProblem, Long> {

    // 대회 문제 목록 조회 - 해당 대회의 문제를 순서대로 전부 조회 (N+1 방지를 위해 Problem 함께 조회)
    @EntityGraph(attributePaths = "problem")
    List<CompetitionProblem> findByCompetitionOrderByProblemOrderAsc(Competition competition);

    // 문제 상세 조회 - 문제 id + 해당 대회 소속인지까지 함께 검증 (N+1 방지를 위해 Problem 함께 조회)
    @EntityGraph(attributePaths = "problem")
    Optional<CompetitionProblem> findByIdAndCompetition(Long id, Competition competition);

    // 마이페이지 - 여러 대회의 모든 문제를 조회 -> 분야별 집계 (N+1 방지를 위해 Problem 함께 조회)
    @EntityGraph(attributePaths = "problem")
    List<CompetitionProblem> findByCompetitionIn(List<Competition> competitions);

    // 마이페이지 - 오답노트. 오답 또는 미응답 문제를 페이지 단위로 조회
    @Query(
            value = """
                    SELECT competitionProblem
                    FROM CompetitionProblem competitionProblem
                    JOIN FETCH competitionProblem.problem
                    JOIN FETCH competitionProblem.competition competition
                    JOIN CompetitionParticipant participant
                      ON participant.competition = competition
                    LEFT JOIN CompetitionProblemSubmission submission
                      ON submission.competitionParticipant = participant
                     AND submission.competitionProblem = competitionProblem
                    WHERE participant.memberId = :memberId
                      AND participant.submittedAt IS NOT NULL
                      AND (submission.id IS NULL OR submission.isCorrect = false)
                    ORDER BY participant.submittedAt DESC, competitionProblem.id DESC
                    """,
            countQuery = """
                    SELECT COUNT(competitionProblem)
                    FROM CompetitionProblem competitionProblem
                    JOIN competitionProblem.competition competition
                    JOIN CompetitionParticipant participant
                      ON participant.competition = competition
                    LEFT JOIN CompetitionProblemSubmission submission
                      ON submission.competitionParticipant = participant
                     AND submission.competitionProblem = competitionProblem
                    WHERE participant.memberId = :memberId
                      AND participant.submittedAt IS NOT NULL
                      AND (submission.id IS NULL OR submission.isCorrect = false)
                    """
    )
    Page<CompetitionProblem> findErrorNoteProblemsByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    // 마이페이지 - 북마크페이지. problemId를 이용해 대회 정보 조회
    @Query("""
            SELECT competitionProblem
            FROM CompetitionProblem competitionProblem
            JOIN FETCH competitionProblem.problem problem
            JOIN FETCH competitionProblem.competition competition
            JOIN CompetitionParticipant participant
              ON participant.competition = competition
            WHERE participant.memberId = :memberId
              AND participant.submittedAt IS NOT NULL
              AND problem.id IN :problemIds
            ORDER BY participant.submittedAt DESC, competitionProblem.id DESC
            """)
    List<CompetitionProblem> findCompletedByMemberIdAndProblemIds(
            @Param("memberId") Long memberId,
            @Param("problemIds") List<Long> problemIds
    );
}
