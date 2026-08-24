package com.example.rootin.ranking.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.exception.CompetitionNotFoundException;
import com.example.rootin.competition.exception.CompetitionParticipantNotFoundException;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.competition.repository.CompetitionProblemSubmissionRepository;
import com.example.rootin.competition.repository.CompetitionRepository;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import com.example.rootin.ranking.domain.RankingPeriod;
import com.example.rootin.ranking.dto.response.CompetitionRankingItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompetitionRankingService {

    private static final int TOP3_SIZE = 3;

    private final CompetitionRepository competitionRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionProblemSubmissionRepository competitionProblemSubmissionRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<CompetitionRankingItemResponse> getTop3(Long competitionId) {
        return buildRanking(competitionId, RankingPeriod.DAILY).stream()
                .limit(TOP3_SIZE)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CompetitionRankingItemResponse> getRankings(Long competitionId, RankingPeriod period, Pageable pageable) {
        List<CompetitionRankingItemResponse> ranking = buildRanking(competitionId, period);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), ranking.size());
        List<CompetitionRankingItemResponse> content = start >= ranking.size() ? List.of() : ranking.subList(start, end);

        Page<CompetitionRankingItemResponse> page = new PageImpl<>(content, pageable, ranking.size());
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public CompetitionRankingItemResponse getMyRanking(Long competitionId, RankingPeriod period, Long memberId) {
        return buildRanking(competitionId, period).stream()
                .filter(item -> item.memberId().equals(memberId))
                .findFirst()
                .orElseThrow(CompetitionParticipantNotFoundException::new);
    }

    // 기간(일간/주간/월간)에 해당하는 대회들의 참여자를 모아서 회원별 점수 합산 후 순위를 매김
    private List<CompetitionRankingItemResponse> buildRanking(Long competitionId, RankingPeriod period) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(CompetitionNotFoundException::new);

        List<Competition> competitions = resolveCompetitions(competition, period);
        List<CompetitionParticipant> participants = competitionParticipantRepository.findByCompetitionIn(competitions);

        Map<Long, Integer> scoreByMember = new HashMap<>();
        Map<Long, LocalDateTime> earliestSubmittedAtByMember = new HashMap<>();

        for (CompetitionParticipant participant : participants) {
            int correctCount = competitionProblemSubmissionRepository
                    .countByCompetitionParticipantAndIsCorrectTrue(participant);
            int score = correctCount * 10;

            scoreByMember.merge(participant.getMemberId(), score, Integer::sum);

            if (participant.getSubmittedAt() != null) {
                earliestSubmittedAtByMember.merge(
                        participant.getMemberId(),
                        participant.getSubmittedAt(),
                        (a, b) -> a.isBefore(b) ? a : b
                );
            }
        }

        List<Long> orderedMemberIds = scoreByMember.entrySet().stream()
                .sorted(
                        Map.Entry.<Long, Integer>comparingByValue().reversed()
                                .thenComparing(entry -> earliestSubmittedAtByMember.getOrDefault(entry.getKey(), LocalDateTime.MAX))
                )
                .map(Map.Entry::getKey)
                .toList();

        List<CompetitionRankingItemResponse> ranking = new ArrayList<>();
        int rank = 1;
        for (Long memberId : orderedMemberIds) {
            Member member = memberRepository.findById(memberId).orElse(null);
            ranking.add(new CompetitionRankingItemResponse(
                    rank++,
                    memberId,
                    member != null ? member.getNickname() : null,
                    member != null ? member.getImgUrl() : null,
                    scoreByMember.get(memberId),
                    earliestSubmittedAtByMember.get(memberId)
            ));
        }

        return ranking;
    }


    private List<Competition> resolveCompetitions(Competition competition, RankingPeriod period) {
        return switch (period) {
            case DAILY -> List.of(competition);
            case WEEKLY -> {
                LocalDate start = competition.getCompetitionDate().with(DayOfWeek.MONDAY);
                LocalDate end = start.plusDays(6);
                yield competitionRepository.findByCompetitionDateBetween(start, end);
            }
            case MONTHLY -> {
                LocalDate start = competition.getCompetitionDate().withDayOfMonth(1);
                LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                yield competitionRepository.findByCompetitionDateBetween(start, end);
            }
        };
    }
}