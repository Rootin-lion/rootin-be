package com.example.rootin.ranking.scheduler;

import com.example.rootin.ranking.service.RankingPointSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RankingPointScheduler {

    private final RankingPointSettlementService rankingPointSettlementService;

    // 매시 1분 - 종료됐는데 정산 안 된 대회를 모두 정산 (서버 재시작 등으로 놓친 대회도 다음 실행에서 처리)
    @Scheduled(cron = "0 1 * * * *", zone = "Asia/Seoul")
    public void settleRankingPoints() {
        rankingPointSettlementService.settleClosedCompetitions(LocalDateTime.now());
    }
}
