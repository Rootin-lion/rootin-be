package com.example.rootin.competition.domain;

public enum CompetitionStatus {
    BEFORE_START, // 대회 시작 전 (00:00 ~ start_at)
    IN_PROGRESS,  // 진행중 (start_at ~ end_at)
    CLOSED        // 종료 (end_at ~ 23:59)
}