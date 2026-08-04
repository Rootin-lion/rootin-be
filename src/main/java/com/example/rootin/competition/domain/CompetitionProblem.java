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

    @Column(name = "problem_content", nullable = false, length = 200)
    private String problemContent;

    @Column(name = "problem_order", nullable = false)
    private Integer problemOrder;

    @Column(name = "problem_explanation", nullable = false, length = 300)
    private String problemExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private InterestField category;

    public CompetitionProblem(Competition competition, String problemContent, Integer problemOrder, String problemExplanation, InterestField category) {
        this.competition = competition;
        this.problemContent = problemContent;
        this.problemOrder = problemOrder;
        this.problemExplanation = problemExplanation;
        this.category = category;
    }
}