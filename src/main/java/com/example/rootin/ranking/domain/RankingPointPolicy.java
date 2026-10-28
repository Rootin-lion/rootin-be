package com.example.rootin.ranking.domain;

// 일간 순위별 지급 포인트 - 지급 기준이 바뀌면 여기만 수정
public final class RankingPointPolicy {

    private static final int FIRST_PLACE_POINT = 30;
    private static final int SECOND_PLACE_POINT = 20;
    private static final int THIRD_PLACE_POINT = 10;
    private static final int PARTICIPATION_POINT = 5;

    private RankingPointPolicy() {
    }

    public static int pointOf(int dailyRank) {
        return switch (dailyRank) {
            case 1 -> FIRST_PLACE_POINT;
            case 2 -> SECOND_PLACE_POINT;
            case 3 -> THIRD_PLACE_POINT;
            default -> PARTICIPATION_POINT;
        };
    }
}
