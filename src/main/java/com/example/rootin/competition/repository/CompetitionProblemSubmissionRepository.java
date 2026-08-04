package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompetitionProblemSubmissionRepository extends JpaRepository<CompetitionProblemSubmission, Long> {

    // 답안 저장 시 기존 답안 존재 여부 확인
    Optional<CompetitionProblemSubmission> findByCompetitionParticipantAndCompetitionProblem(
            CompetitionParticipant competitionParticipant,
            CompetitionProblem competitionProblem
    );
}

