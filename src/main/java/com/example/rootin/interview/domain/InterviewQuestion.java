package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "interview_question")
@NoArgsConstructor
public class InterviewQuestion {
    //질문 이력, 답변 이력, 키워드 스냅샷, 점수, 피드백 저장

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_session_topic_id", nullable = false)
    private InterviewSessionTopic sessionTopic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_topic_id", nullable = false)
    private InterviewTopic interviewTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private InterviewQuestionType questionType;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interview_question_target_keywords", joinColumns = @JoinColumn(name = "interview_question_id"))
    @Column(name = "keyword", nullable = false)
    private List<String> targetKeywords;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private InterviewQuestion(
            Interview interview,
            InterviewSessionTopic sessionTopic,
            InterviewTopic interviewTopic,
            InterviewQuestionType questionType,
            int questionOrder,
            String questionText,
            List<String> targetKeywords
    ) {
        this.interview = interview;
        this.sessionTopic = sessionTopic;
        this.interviewTopic = interviewTopic;
        this.questionType = questionType;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.targetKeywords = targetKeywords;
        this.createdAt = LocalDateTime.now();
    }

    public static InterviewQuestion create(
            Interview interview,
            InterviewSessionTopic sessionTopic,
            InterviewTopic interviewTopic,
            InterviewQuestionType questionType,
            int questionOrder,
            String questionText,
            List<String> targetKeywords
    ) {
        return new InterviewQuestion(
                interview,
                sessionTopic,
                interviewTopic,
                questionType,
                questionOrder,
                questionText,
                targetKeywords
        );
    }
}
