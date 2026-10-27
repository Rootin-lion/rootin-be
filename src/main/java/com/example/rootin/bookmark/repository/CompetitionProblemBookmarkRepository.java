package com.example.rootin.bookmark.repository;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.competition.domain.Problem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionProblemBookmarkRepository extends JpaRepository<CompetitionProblemBookmark, Long> {

    // 북마크 설정/해제 시 이미 북마크했는지 확인하는 용도
    Optional<CompetitionProblemBookmark> findByMemberIdAndProblem(Long memberId, Problem problem);

    // 마이페이지 - 북마크한 문제를 최신 순으로 조회
    @EntityGraph(attributePaths = "problem")
    Page<CompetitionProblemBookmark> findByMemberIdOrderByCreatedAtDescIdDesc(
            Long memberId,
            Pageable pageable
    );

    boolean existsByMemberIdAndProblem(Long memberId, Problem problem);
}
