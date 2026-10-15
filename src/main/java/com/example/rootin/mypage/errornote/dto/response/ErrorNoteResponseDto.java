package com.example.rootin.mypage.errornote.dto.response;

import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import com.example.rootin.competition.domain.Problem;
import com.example.rootin.competition.domain.ProblemOption;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDate;

public record ErrorNoteResponseDto(
        Long competitionId,
        Long competitionProblemId,
        InterestField category,
        String title,
        LocalDate participationDate,
        String problemContent,
        String myAnswer,
        String correctAnswer,
        String explanation
) {
    public static ErrorNoteResponseDto from(
            CompetitionProblem competitionProblem,
            CompetitionProblemSubmission submission,
            ProblemOption correctOption
    ) {
        Problem problem = competitionProblem.getProblem();

        return new ErrorNoteResponseDto(
                competitionProblem.getCompetition().getId(),
                competitionProblem.getId(),
                problem.getCategory(),
                problem.getTitle(),
                competitionProblem.getCompetition().getCompetitionDate(),
                problem.getContent(),
                submission == null ? null : submission.getSelectedOption().getOptionContent(),
                correctOption.getOptionContent(),
                problem.getExplanation()
        );
    }
}
