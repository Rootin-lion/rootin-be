package com.example.rootin.competition.domain;

import com.example.rootin.member.domain.InterestField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "competition_problem")
@Getter
@NoArgsConstructor
public class CompetitionProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "problem_order", nullable = false)
    private Integer problemOrder;

    public CompetitionProblem(Competition competition, Problem problem, Integer problemOrder) {
        this.competition = competition;
        this.problem = problem;
        this.problemOrder = problemOrder;
    }
}