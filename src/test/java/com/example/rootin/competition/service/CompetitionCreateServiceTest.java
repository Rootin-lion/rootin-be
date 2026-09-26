package com.example.rootin.competition.service;

import com.example.rootin.competition.domain.Competition;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.competition.repository.CompetitionRepository;
import com.example.rootin.member.domain.InterestField;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CompetitionCreateServiceTest {

    @Autowired CompetitionCreateService competitionCreateService;
    @Autowired CompetitionRepository competitionRepository;
    @Autowired CompetitionProblemRepository competitionProblemRepository;

    private static final LocalDate TEST_DATE = LocalDate.of(2026, 10, 1);

    @Test
    @DisplayName("대회 생성 시 10문제가 출제되고, 모든 분야가 1~2문제씩 포함된다")
    void createDailyCompetition() {
        competitionCreateService.createDailyCompetition(TEST_DATE);

        List<CompetitionProblem> problems = findProblems(TEST_DATE);
        Map<InterestField, Long> countByCategory = problems.stream()
                .collect(Collectors.groupingBy(cp -> cp.getProblem().getCategory(), Collectors.counting()));

        assertThat(problems).hasSize(10);
        assertThat(countByCategory).hasSize(InterestField.values().length);
        assertThat(countByCategory.values()).allMatch(count -> count >= 1 && count <= 2);
        // 같은 대회 안에 중복 문제 없음
        assertThat(problems).extracting(cp -> cp.getProblem().getId()).doesNotHaveDuplicates();
        // 순서 1~10
        assertThat(problems).extracting(CompetitionProblem::getProblemOrder)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    @DisplayName("같은 날짜로 두 번 호출해도 대회는 하나만 생성된다")
    void createDailyCompetition_duplicate() {
        competitionCreateService.createDailyCompetition(TEST_DATE);
        competitionCreateService.createDailyCompetition(TEST_DATE);

        assertThat(competitionRepository.findByCompetitionDateBetween(TEST_DATE, TEST_DATE)).hasSize(1);
        assertThat(findProblems(TEST_DATE)).hasSize(10);
    }

    @Test
    @DisplayName("연속된 날짜로 생성해도 각 대회는 10문제를 유지한다 (부족하면 보충)")
    void createDailyCompetition_consecutiveDays() {
        for (int i = 0; i < 3; i++) {
            competitionCreateService.createDailyCompetition(TEST_DATE.plusDays(i));
        }

        for (int i = 0; i < 3; i++) {
            List<CompetitionProblem> problems = findProblems(TEST_DATE.plusDays(i));
            System.out.println(TEST_DATE.plusDays(i) + " 출제: " +
                    problems.stream().map(cp -> cp.getProblem().getId()).toList());
            assertThat(problems).hasSize(10);
        }
    }

    private List<CompetitionProblem> findProblems(LocalDate date) {
        Competition competition = competitionRepository.findByCompetitionDate(date).orElseThrow();
        return competitionProblemRepository.findByCompetitionOrderByProblemOrderAsc(competition);
    }
}