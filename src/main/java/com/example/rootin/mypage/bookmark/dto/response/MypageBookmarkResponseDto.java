package com.example.rootin.mypage.bookmark.dto.response;

import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.Problem;
import com.example.rootin.member.domain.InterestField;

import java.time.LocalDate;

public record MypageBookmarkResponseDto(
        Long competitionId,
        Long competitionProblemId,
        Long problemId,
        InterestField category,
        String title,
        LocalDate participationDate,
        String problemContent
) {
    public static MypageBookmarkResponseDto from(CompetitionProblem competitionProblem) {
        Problem problem = competitionProblem.getProblem();

        return new MypageBookmarkResponseDto(
                competitionProblem.getCompetition().getId(),
                competitionProblem.getId(),
                problem.getId(),
                problem.getCategory(),
                problem.getTitle(),
                competitionProblem.getCompetition().getCompetitionDate(),
                problem.getContent()
        );
    }
}
