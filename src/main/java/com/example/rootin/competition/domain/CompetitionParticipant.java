package com.example.rootin.competition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "competition_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "competition_id"})
)
@Getter
@NoArgsConstructor
public class CompetitionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // 대회 종료 후 확정된 랭킹 포인트 (정산 전이면 null)
    @Column(name = "reward_point")
    private Integer rewardPoint;

    public CompetitionParticipant(Long memberId, Competition competition, LocalDateTime startedAt) {
        this.memberId = memberId;
        this.competition = competition;
        this.startedAt = startedAt;
    }

    public void submit(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void grantRewardPoint(int rewardPoint) {
        this.rewardPoint = rewardPoint;
    }
}
