package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionParticipantRepository extends JpaRepository<CompetitionParticipant, Long> {

    Optional<CompetitionParticipant> findByMemberIdAndCompetition(Long memberId, Competition competition);
}