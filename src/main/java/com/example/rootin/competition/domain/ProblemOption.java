package com.example.rootin.competition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problem_option")
@Getter
@NoArgsConstructor
public class ProblemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "option_content", nullable = false, length = 255)
    private String optionContent;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "is_answer", nullable = false)
    private boolean isAnswer = false;

    public ProblemOption(Problem problem, String optionContent, Integer optionOrder, boolean isAnswer) {
        this.problem = problem;
        this.optionContent = optionContent;
        this.optionOrder = optionOrder;
        this.isAnswer = isAnswer;
    }
}