package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionParticipantRepository extends JpaRepository<CompetitionParticipant, Long> {

    Optional<CompetitionParticipant> findByMemberIdAndCompetition(Long memberId, Competition competition);

    // 종료된 대회 목록 - "결과 보기" 활성화 여부(대회에 참여했었는지 판단용)
    boolean existsByMemberIdAndCompetition(Long memberId, Competition competition);

    // 종료된 대회 목록 - 참가자 수
    long countByCompetition(Competition competition);
}