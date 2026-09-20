package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interviewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewReportStatus status;

    private int averageAccuracy;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @Column(name = "strenghts", columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public static InterviewReport create(Interview interviewId) {

        InterviewReport report = new InterviewReport();

        report.interviewId = interviewId;
        report.status = InterviewReportStatus.GENERATING;
        report.createdAt = LocalDateTime.now();

        return report;
    }

    public void complete(
            int averageAccuracy,
            String overallFeedback,
            String strengths,
            String weaknesses
    ) {
        this.averageAccuracy = averageAccuracy;
        this.overallFeedback = overallFeedback;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.status = InterviewReportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = InterviewReportStatus.FAILED;
    }
}
