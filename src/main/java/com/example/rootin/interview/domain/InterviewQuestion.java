package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "interview_question")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private InterviewTopic topicId;

    @OneToOne(mappedBy = "questionId", fetch = FetchType.LAZY)
    private InterviewAnswer answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "target_keywords", nullable = false)
    private String targetKeywords;

    @Column(name = "evaluation_criteria", nullable = false)
    private String evaluationCriteria;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    public static InterviewQuestion create(
            Interview interviewId,
            InterviewTopic topicId,
            QuestionType questionType,
            String question,
            String targetKeywords,
            String evaluationCriteria,
            int questionOrder
    ) {
        InterviewQuestion interviewQuestion = new InterviewQuestion();

        interviewQuestion.interviewId = interviewId;
        interviewQuestion.topicId = topicId;
        interviewQuestion.questionType = questionType;
        interviewQuestion.question = question;
        interviewQuestion.targetKeywords = targetKeywords;
        interviewQuestion.evaluationCriteria = evaluationCriteria;
        interviewQuestion.questionOrder = questionOrder;

        return interviewQuestion;
    }
}
