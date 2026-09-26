package com.example.rootin.bookmark.domain;

import com.example.rootin.competition.domain.Problem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition_problem_bookmark",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "competition_problem_id"}))
@Getter
@NoArgsConstructor
public class CompetitionProblemBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CompetitionProblemBookmark(Long memberId, Problem problem, LocalDateTime createdAt) {
        this.memberId = memberId;
        this.problem = problem;
        this.createdAt = createdAt;
    }
}