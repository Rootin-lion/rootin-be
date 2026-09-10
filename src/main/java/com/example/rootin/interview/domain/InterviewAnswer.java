package com.example.rootin.interview.domain;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "interview_answer")
@NoArgsConstructor
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "interview_question_id",
            nullable = false,
            unique = true
    )
    private InterviewQuestion interviewQuestion;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "interview_answer_matched_keywords",
            joinColumns = @JoinColumn(name = "interview_answer_id")
    )
    @Column(name = "keyword", nullable = false)
    private List<String> matchedKeywords;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "interview_answer_missing_keywords",
            joinColumns = @JoinColumn(name = "interview_answer_id")
    )
    @Column(name = "keyword", nullable = false)
    private List<String> missingKeywords;

    @Column(name = "keyword_coverage")
    private Double keywordCoverage;

    @Column(name = "score")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation", length = 20)
    private AnswerEvaluation evaluation;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    private InterviewAnswer(
            InterviewQuestion interviewQuestion,
            String answerText,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            Double keywordCoverage,
            Integer score,
            AnswerEvaluation evaluation,
            String feedback,
            LocalDateTime answeredAt
    ) {
        this.interviewQuestion = interviewQuestion;
        this.answerText = answerText;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.keywordCoverage = keywordCoverage;
        this.score = score;
        this.evaluation = evaluation;
        this.feedback = feedback;
        this.answeredAt = answeredAt;
    }

    public static InterviewAnswer create(
            InterviewQuestion interviewQuestion,
            String answerText,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            Double keywordCoverage,
            Integer score,
            AnswerEvaluation evaluation,
            String feedback
    ) {
        return new InterviewAnswer(
                interviewQuestion,
                answerText,
                matchedKeywords,
                missingKeywords,
                keywordCoverage,
                score,
                evaluation,
                feedback,
                LocalDateTime.now()
        );
    }
}
