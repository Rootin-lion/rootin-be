package com.example.rootin.competition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "competition_problem_option")
@Getter
@NoArgsConstructor
public class CompetitionProblemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_problem_id", nullable = false)
    private CompetitionProblem competitionProblem;

    @Column(name = "option_content", nullable = false, length = 255)
    private String optionContent;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "is_answer", nullable = false)
    private boolean isAnswer = false;

    public CompetitionProblemOption(CompetitionProblem competitionProblem, String optionContent, Integer optionOrder, boolean isAnswer) {
        this.competitionProblem = competitionProblem;
        this.optionContent = optionContent;
        this.optionOrder = optionOrder;
        this.isAnswer = isAnswer;
    }
}