package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "interview_session_topic")
@NoArgsConstructor
public class InterviewSessionTopic {
    //세션에서 사용할 topic 순서, 완료여부, BASIC/FOLLOW_UP 생성 수 추적

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_topic_id", nullable = false)
    private InterviewTopic interviewTopic;

    @Column(name = "topic_order", nullable = false)
    private int topicOrder;

    @Column(name = "basic_question_count", nullable = false)
    private int basicQuestionCount;

    @Column(name = "follow_up_question_count", nullable = false)
    private int followUpQuestionCount;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private InterviewSessionTopic(
            Interview interview,
            InterviewTopic interviewTopic,
            int topicOrder,
            LocalDateTime startedAt
    ) {
        this.interview = interview;
        this.interviewTopic = interviewTopic;
        this.topicOrder = topicOrder;
        this.startedAt = startedAt;
        this.basicQuestionCount = 0;
        this.followUpQuestionCount = 0;
        this.completed = false;
    }

    public static InterviewSessionTopic create(
            Interview interview,
            InterviewTopic interviewTopic,
            int topicOrder
    ) {
        return new InterviewSessionTopic(interview, interviewTopic, topicOrder, LocalDateTime.now());
    }

    public void incrementBasicQuestionCount() {
        this.basicQuestionCount++;
    }

    public void incrementFollowUpQuestionCount() {
        this.followUpQuestionCount++;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
}
