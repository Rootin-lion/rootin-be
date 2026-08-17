package com.example.rootin.bookmark.repository;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.competition.domain.CompetitionProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionProblemBookmarkRepository extends JpaRepository<CompetitionProblemBookmark, Long> {

    // 북마크 설정/해제 시 이미 북마크했는지 확인하는 용도
    Optional<CompetitionProblemBookmark> findByMemberIdAndCompetitionProblem(Long memberId, CompetitionProblem competitionProblem);
}