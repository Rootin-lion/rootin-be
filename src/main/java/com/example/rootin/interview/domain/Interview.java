package com.example.rootin.interview.domain;

import com.example.rootin.member.domain.InterestField;
import com.example.rootin.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "interview")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InterestField category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "question_count" ,nullable = false)
    private int questionCount;

    @Column(name = "current_topic_order",nullable = false)
    private int currentTopicOrder;

    @ElementCollection
    @CollectionTable(
            name = "interview_topic_order",
            joinColumns = @JoinColumn(name = "interview_id")
    )
    @Column(name = "topic_id")
    private List<Long> topicIds = new ArrayList<>(); //면접시 물어볼 토픽 리스트(순서)

    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    public static Interview create(
            Member member,
            InterestField category,
            int totalTopicCount,
            List<Long> topicIds
    ) {

        Interview interview = new Interview();

        interview.member = member;
        interview.category = category;
        interview.status = InterviewStatus.IN_PROGRESS;
        interview.currentTopicOrder = 1;
        interview.questionCount = totalTopicCount;
        interview.topicIds = topicIds;

        return interview;
    }

    public Long getCurrentTopicId() {
        return topicIds.get(currentTopicOrder - 1);
    }

    public boolean isLastTopic() {
        return currentTopicOrder == questionCount;
    }

    public void moveToNextTopic() {
        this.currentTopicOrder++;
    }

    public void complete() {
        this.status = InterviewStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
