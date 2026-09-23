package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class InterviewEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private InterviewAnswer answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_level", nullable = false)
    private AnswerLevel answerLevel;

    private int accuracy;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords;

    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    public static InterviewEvaluation create(InterviewAnswer answer, AnswerLevel answerLevel, int accuracy,
                                             String feedback, String matchedKeywords, String missingKeywords) {

        InterviewEvaluation evaluation = new InterviewEvaluation();

        evaluation.answer = answer;
        evaluation.answerLevel = answerLevel;
        evaluation.accuracy = accuracy;
        evaluation.feedback = feedback;
        evaluation.matchedKeywords = matchedKeywords;
        evaluation.missingKeywords = missingKeywords;

        return evaluation;
    }
}
