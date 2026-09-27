package com.example.rootin.bookmark.repository;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.competition.domain.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompetitionProblemBookmarkRepository extends JpaRepository<CompetitionProblemBookmark, Long> {

    // 북마크 설정/해제 시 이미 북마크했는지 확인하는 용도
    Optional<CompetitionProblemBookmark> findByMemberIdAndProblem(Long memberId, Problem problem);

    // 마이페이지 - 북마크한 문제를 최신 순으로 조회
    @Query("""
            SELECT bookmark
            FROM CompetitionProblemBookmark bookmark
            JOIN FETCH bookmark.problem
            WHERE bookmark.memberId = :memberId
            ORDER BY bookmark.createdAt DESC, bookmark.id DESC
            """)
    List<CompetitionProblemBookmark> findRecentByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
