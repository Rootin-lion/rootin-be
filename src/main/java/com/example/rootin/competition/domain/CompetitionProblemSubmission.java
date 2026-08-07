package com.example.rootin.competition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "competition_problem_submission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"competition_participant_id", "competition_problem_id"})
)
@Getter
@NoArgsConstructor
public class CompetitionProblemSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    private CompetitionProblemOption selectedOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_participant_id", nullable = false)
    private CompetitionParticipant competitionParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_problem_id", nullable = false)
    private CompetitionProblem competitionProblem;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    public CompetitionProblemSubmission(
            CompetitionProblemOption selectedOption,
            CompetitionParticipant competitionParticipant,
            CompetitionProblem competitionProblem,
            boolean isCorrect
    ) {
        this.selectedOption = selectedOption;
        this.competitionParticipant = competitionParticipant;
        this.competitionProblem = competitionProblem;
        this.isCorrect = isCorrect;
    }

    // 답안 변경 - 최종 제출 전까지만 호출 가능
    public void changeAnswer(CompetitionProblemOption selectedOption, boolean isCorrect) {
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
    }
}