package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.*;
import com.example.rootin.competition.dto.response.CompetitionResultResponse;
import com.example.rootin.competition.exception.CompetitionNotFoundException;
import com.example.rootin.competition.exception.CompetitionNotSubmittedException;
import com.example.rootin.competition.exception.CompetitionParticipantNotFoundException;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.competition.repository.CompetitionProblemSubmissionRepository;
import com.example.rootin.competition.repository.CompetitionRepository;
import com.example.rootin.member.domain.InterestField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetitionResultService {

    // TODO: CompetitionService의 PROBLEM_COUNT와 중복 -> 리팩토링할 때 공통 상수로 정리할 것
    private static final int PROBLEM_COUNT = 10;

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionProblemRepository competitionProblemRepository;
    private final CompetitionProblemSubmissionRepository competitionProblemSubmissionRepository;

    @Transactional(readOnly = true)
    public CompetitionResultResponse getResult(Long competitionId, Long memberId) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        CompetitionParticipant participant = competitionParticipantRepository
                .findByMemberIdAndCompetition(memberId, competition)
                .orElseThrow(CompetitionParticipantNotFoundException::new);

        if (participant.getSubmittedAt() == null) {
            throw new CompetitionNotSubmittedException();
        }

        List<CompetitionProblem> problems = competitionProblemRepository.findByCompetitionOrderByProblemOrderAsc(competition);

        Map<Long, CompetitionProblemSubmission> submissionByProblemId =
                competitionProblemSubmissionRepository.findByCompetitionParticipant(participant).stream()
                        .collect(Collectors.toMap(
                                submission -> submission.getCompetitionProblem().getId(),
                                submission -> submission
                        ));

        // 카테고리별 정답/전체 문제 개수
        Map<InterestField, int[]> categoryStats = new EnumMap<>(InterestField.class);

        List<CompetitionResultResponse.ProblemResultResponse> problemResults = problems.stream()
                .map(problem -> {
                    CompetitionProblemSubmission submission = submissionByProblemId.get(problem.getId());
                    ProblemResultStatus status = submission == null
                            ? ProblemResultStatus.UNANSWERED
                            : submission.isCorrect() ? ProblemResultStatus.CORRECT : ProblemResultStatus.WRONG;

                    int[] stats = categoryStats.computeIfAbsent(problem.getCategory(), c -> new int[2]);
                    stats[1]++; // 해당 카테고리 전체 문제 개수
                    if (status == ProblemResultStatus.CORRECT) {
                        stats[0]++; // 해당 카테고리 정답 개수
                    }

                    return new CompetitionResultResponse.ProblemResultResponse(
                            problem.getProblemOrder(),
                            problem.getId(),
                            status,
                            status == ProblemResultStatus.CORRECT ? 10 : 0
                    );
                })
                .toList();

        int correctCount = (int) problemResults.stream()
                .filter(result -> result.status() == ProblemResultStatus.CORRECT)
                .count();

        List<InterestField> strongCategories = pickCategoriesByAccuracy(categoryStats, true);
        List<InterestField> weakCategories = pickCategoriesByAccuracy(categoryStats, false);

        long solvingTimeSeconds = Duration.between(participant.getStartedAt(), participant.getSubmittedAt()).getSeconds();

        return new CompetitionResultResponse(
                competition.getCompetitionDate(),
                correctCount * 10,
                PROBLEM_COUNT * 10,
                correctCount,
                PROBLEM_COUNT,
                solvingTimeSeconds,
                problemResults,
                strongCategories,
                weakCategories
        );
    }

    private List<InterestField> pickCategoriesByAccuracy(Map<InterestField, int[]> categoryStats, boolean strongest) {
        double targetAccuracy = categoryStats.values().stream()
                .mapToDouble(stats -> (double) stats[0] / stats[1])
                .reduce(strongest ? Double::max : Double::min)
                .orElse(0.0);

        return categoryStats.entrySet().stream()
                .filter(entry -> (double) entry.getValue()[0] / entry.getValue()[1] == targetAccuracy)
                .map(Map.Entry::getKey)
                .toList();
    }
}
