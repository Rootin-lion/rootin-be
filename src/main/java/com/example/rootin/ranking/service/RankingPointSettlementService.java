package com.example.rootin.ranking.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.member.repository.MemberRepository;
import com.example.rootin.ranking.domain.RankingPeriod;
import com.example.rootin.ranking.dto.response.CompetitionRankingItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 대회 종료 후 최종 일간 순위로 포인트를 확정하고 회원 포인트에 적립
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingPointSettlementService {

    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final MemberRepository memberRepository;
    private final CompetitionRankingService competitionRankingService;

    @Transactional
    public void settleClosedCompetitions(LocalDateTime now) {
        List<Competition> competitions = competitionParticipantRepository.findUnsettledClosedCompetitions(now);
        competitions.forEach(this::settle);
    }

    private void settle(Competition competition) {
        Map<Long, Integer> pointByMember = competitionRankingService
                .getRankingItems(competition.getId(), RankingPeriod.DAILY).stream()
                .collect(Collectors.toMap(CompetitionRankingItemResponse::memberId, CompetitionRankingItemResponse::point));

        List<CompetitionParticipant> participants =
                competitionParticipantRepository.findByCompetitionInAndSubmittedAtIsNotNull(List.of(competition));

        int settledCount = 0;
        for (CompetitionParticipant participant : participants) {
            if (participant.getRewardPoint() != null) {
                continue; // 이미 정산된 참여자는 중복 지급하지 않음
            }
            int point = pointByMember.getOrDefault(participant.getMemberId(), 0);
            participant.grantRewardPoint(point);
            memberRepository.findById(participant.getMemberId())
                    .ifPresent(member -> member.addPoint(point));
            settledCount++;
        }
        log.info("랭킹 포인트 정산 완료 - competitionId={}, 정산 인원={}", competition.getId(), settledCount);
    }
}
