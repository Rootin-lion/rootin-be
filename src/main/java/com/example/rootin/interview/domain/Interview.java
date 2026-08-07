package com.example.rootin.interview.domain;

import com.example.rootin.member.domain.InterestField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "interview")
@NoArgsConstructor
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "field", nullable = false)
    private InterestField field;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_mode", nullable = false)
    private InterviewMode interviewMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;


    private Interview(Long memberId, InterestField field, int questionCount, InterviewMode interviewMode,
                      InterviewStatus status, LocalDateTime startedAt) {
        this.memberId = memberId;
        this.field = field;
        this.questionCount = questionCount;
        this.interviewMode = interviewMode;
        this.status = status;
        this.startedAt = startedAt;
    }

    public static Interview create(Long memberId, InterestField field, int questionCount, InterviewMode interviewMode) {
        return new Interview(
                memberId,
                field,
                questionCount,
                interviewMode,
                InterviewStatus.CREATED,
                LocalDateTime.now()
        );
    }
}
