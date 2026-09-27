package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.Problem;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.competition.repository.CompetitionRepository;
import com.example.rootin.competition.repository.ProblemRepository;
import com.example.rootin.member.domain.InterestField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetitionCreateService {

    private static final LocalTime START_TIME = LocalTime.of(12, 0);
    private static final LocalTime END_TIME = LocalTime.of(13, 0);
    private static final int RECENT_EXCLUDE_DAYS = 7; // 최근 7일 출제 문제 제외
    private static final int EXTRA_CATEGORY_COUNT = 4; // 6개 분야 1문제씩 + 4개 분야 1문제 추가 = 10문제

    private final CompetitionRepository competitionRepository;
    private final CompetitionProblemRepository competitionProblemRepository;
    private final ProblemRepository problemRepository;

    @Transactional
    public void createDailyCompetition(LocalDate today) {
        // 1. 오늘 대회가 이미 있으면 생성하지 않음 (재시작/재배포 시 중복 방지)
        if (competitionRepository.existsByCompetitionDate(today)) {
            log.info("[대회 생성] {} 대회가 이미 존재하여 생성을 건너뜁니다.", today);
            return;
        }

        // 2. 대회 생성
        Competition competition = competitionRepository.save(
                new Competition(today, today.atTime(START_TIME), today.atTime(END_TIME)));

        // 3. 분야별 출제 개수 결정 → 4. 분야별 문제 선택
        LocalDate since = today.minusDays(RECENT_EXCLUDE_DAYS);
        List<Problem> selected = new ArrayList<>();
        decideProblemCounts().forEach((category, count) ->
                selected.addAll(pickProblems(category, count, since)));

        // 5. 순서 섞어서 1번부터 출제
        Collections.shuffle(selected);
        List<CompetitionProblem> competitionProblems = IntStream.range(0, selected.size())
                .mapToObj(i -> new CompetitionProblem(competition, selected.get(i), i + 1))
                .toList();
        competitionProblemRepository.saveAll(competitionProblems);

        log.info("[대회 생성] {} 대회 생성 완료 - 문제 {}개", today, competitionProblems.size());
    }

    // 모든 분야 1문제 + 랜덤 4개 분야 1문제 추가
    private Map<InterestField, Integer> decideProblemCounts() {
        List<InterestField> categories = new ArrayList<>(List.of(InterestField.values()));
        Collections.shuffle(categories);

        Map<InterestField, Integer> counts = new EnumMap<>(InterestField.class);
        for (int i = 0; i < categories.size(); i++) {
            counts.put(categories.get(i), i < EXTRA_CATEGORY_COUNT ? 2 : 1);
        }
        return counts;
    }

    // 최근 출제 문제 제외하고 선택, 부족하면 분야 전체에서 보충
    private List<Problem> pickProblems(InterestField category, int count, LocalDate since) {
        List<Problem> picked = new ArrayList<>(
                problemRepository.findRandomExcludingRecent(category.name(), since, count));

        if (picked.size() < count) {
            log.warn("[대회 생성] {} 분야 신규 문제 부족 ({}/{}) - 최근 출제 문제로 보충합니다.",
                    category, picked.size(), count);

            Set<Long> pickedIds = new HashSet<>();
            picked.forEach(p -> pickedIds.add(p.getId()));

            problemRepository.findRandomByCategory(category.name(), count).stream()
                    .filter(p -> !pickedIds.contains(p.getId()))
                    .limit(count - picked.size())
                    .forEach(picked::add);
        }

        if (picked.size() < count) {
            log.error("[대회 생성] {} 분야 문제가 부족합니다. ({}/{}) 문제 은행에 추가가 필요합니다.",
                    category, picked.size(), count);
        }
        return picked;
    }
}
