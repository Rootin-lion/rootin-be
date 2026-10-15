package com.example.rootin.mypage.errornote.service;

import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import com.example.rootin.competition.domain.ProblemOption;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.competition.repository.CompetitionProblemSubmissionRepository;
import com.example.rootin.competition.repository.ProblemOptionRepository;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.mypage.errornote.dto.response.ErrorNoteResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErrorNoteService {

    private static final int ERROR_NOTE_PAGE_SIZE = 4;

    private final CompetitionProblemSubmissionRepository submissionRepository;
    private final CompetitionProblemRepository competitionProblemRepository;
    private final ProblemOptionRepository problemOptionRepository;

    public PageResponse<ErrorNoteResponseDto> getErrorNotes(Long memberId, int page) {
        if (page < 1) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        var competitionProblems = competitionProblemRepository.findErrorNoteProblemsByMemberId(
                memberId,
                PageRequest.of(page - 1, ERROR_NOTE_PAGE_SIZE)
        );

        var competitionProblemIds = competitionProblems.getContent().stream()
                .map(competitionProblem -> competitionProblem.getId())
                .toList();

        var submissionByCompetitionProblemId = competitionProblemIds.isEmpty()
                ? Map.<Long, CompetitionProblemSubmission>of()
                : submissionRepository.findByMemberIdAndCompetitionProblemIds(memberId, competitionProblemIds).stream()
                        .collect(Collectors.toMap(
                                submission -> submission.getCompetitionProblem().getId(),
                                Function.identity()
                        ));

        var problemIds = competitionProblems.getContent().stream()
                .map(competitionProblem -> competitionProblem.getProblem().getId())
                .distinct()
                .toList();

        Map<Long, ProblemOption> correctOptionByProblemId = problemIds.isEmpty()
                ? Map.of()
                : problemOptionRepository.findCorrectOptionsByProblemIds(problemIds).stream()
                        .collect(Collectors.toMap(
                                option -> option.getProblem().getId(),
                                Function.identity()
                        ));

        Page<ErrorNoteResponseDto> responsePage = competitionProblems.map(competitionProblem -> {
            Long problemId = competitionProblem.getProblem().getId();
            ProblemOption correctOption = correctOptionByProblemId.get(problemId);
            if (correctOption == null) {
                throw new CustomException(ErrorCode.NOT_FOUND);
            }
            return ErrorNoteResponseDto.from(
                    competitionProblem,
                    submissionByCompetitionProblemId.get(competitionProblem.getId()),
                    correctOption
            );
        });

        return PageResponse.of(responsePage);
    }
}
