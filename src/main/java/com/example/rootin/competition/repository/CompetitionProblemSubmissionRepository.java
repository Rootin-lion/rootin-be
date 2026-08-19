package com.example.rootin.competition.repository;

import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionProblemSubmissionRepository extends JpaRepository<CompetitionProblemSubmission, Long> {

    // 답안 저장 시 기존 답안 존재 여부 확인
    Optional<CompetitionProblemSubmission> findByCompetitionParticipantAndCompetitionProblem(
            CompetitionParticipant competitionParticipant,
            CompetitionProblem competitionProblem
    );

    // 진행 정보 조회 - 참여자가 지금까지 답한 문제 전체 조회
    List<CompetitionProblemSubmission> findByCompetitionParticipant(CompetitionParticipant competitionParticipant);

    // 랭킹 - 참여자별 정답 개수 (점수 계산용)
    int countByCompetitionParticipantAndIsCorrectTrue(CompetitionParticipant competitionParticipant);
}

