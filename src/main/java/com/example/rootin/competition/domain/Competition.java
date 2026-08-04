package com.example.rootin.competition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "competition")
@Getter
@NoArgsConstructor
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "competition_date", nullable = false)
    private LocalDate competitionDate;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    public Competition(LocalDate competitionDate, LocalDateTime startAt, LocalDateTime endAt) {
        this.competitionDate = competitionDate;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    // 현재 시각 기준 대회 상태 판단
    public CompetitionStatus getStatus(LocalDateTime now) {
        if (now.isBefore(startAt)) {
            return CompetitionStatus.BEFORE_START;
        }
        if (now.isAfter(endAt)) {
            return CompetitionStatus.CLOSED;
        }
        return CompetitionStatus.IN_PROGRESS;
    }
}