package com.example.rootin.mypage.dashboard.service;

import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.competition.repository.CompetitionProblemSubmissionRepository;
import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.interview.repository.InterviewRepository;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.domain.InterestField;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import com.example.rootin.mypage.dashboard.dto.response.CategoryAccuracyResponseDto;
import com.example.rootin.mypage.dashboard.dto.response.DailyActivityResponseDto;
import com.example.rootin.mypage.dashboard.dto.response.DashboardResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionProblemSubmissionRepository competitionProblemSubmissionRepository;
    private final InterviewRepository interviewRepository;
    private final MemberRepository memberRepository;

    public DashboardResponseDto getDashboard(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        List<CompetitionParticipant> completedCompetitions =
                competitionParticipantRepository.findCompletedByMemberId(memberId);
        List<CompetitionProblemSubmission> submissions =
                competitionProblemSubmissionRepository.findCompletedSubmissionsByMemberId(memberId);
        List<Interview> completedInterviews = interviewRepository
                .findByMemberIdAndStatusAndCompletedAtIsNotNull(memberId, InterviewStatus.COMPLETED);

        long totalSolvedCount = submissions.size();
        long correctCount = submissions.stream()
                .filter(CompetitionProblemSubmission::isCorrect)
                .count();

        return new DashboardResponseDto(
                totalSolvedCount,
                calculateRate(correctCount, totalSolvedCount),
                member.getPoint(),
                calculateStreakDays(completedCompetitions),
                createDailyActivities(completedCompetitions, completedInterviews),
                createCategoryAccuracies(submissions)
        );
    }

    private int calculateStreakDays(List<CompetitionParticipant> completedCompetitions ) {
        Set<LocalDate> participationDates = new HashSet<>();
        for (CompetitionParticipant participant : completedCompetitions) {
            participationDates.add(participant.getCompetition().getCompetitionDate());
        }

        LocalDate cursor = LocalDate.now();
        if (!participationDates.contains(cursor)) { //아직 당일 대회 참여 전이라면 어제까지 검사
            cursor = cursor.minusDays(1);
        }

        int streakDays = 0;
        while (participationDates.contains(cursor)) {
            streakDays++;
            cursor = cursor.minusDays(1);
        }
        return streakDays;
    }

    private List<DailyActivityResponseDto> createDailyActivities(
            List<CompetitionParticipant> completedCompetitions,
            List<Interview> completedInterviews
    ) {
        int currentYear = LocalDate.now().getYear();
        Map<LocalDate, ActivityCount> activityCounts = new HashMap<>();

        for (CompetitionParticipant participant : completedCompetitions) {
            LocalDate date = participant.getSubmittedAt().toLocalDate();
            if (date.getYear() == currentYear) {
                activityCounts.computeIfAbsent(date, ignored -> new ActivityCount())
                        .increaseCompetition(); //제출한 대회가 있으면 +1
            }
        }

        for (Interview interview : completedInterviews) {
            LocalDate date = interview.getCompletedAt().toLocalDate();
            if (date.getYear() == currentYear) {
                activityCounts.computeIfAbsent(date, ignored -> new ActivityCount())
                        .increaseInterview(); //완료한 면접이 있으면 +1
            }
        }

        return activityCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) //날짜 오름차순 정렬
                .map(entry -> DailyActivityResponseDto.of(
                        entry.getKey(),
                        entry.getValue().competitionCount,
                        entry.getValue().interviewCount
                ))
                .toList();
    }

    private List<CategoryAccuracyResponseDto> createCategoryAccuracies(
            List<CompetitionProblemSubmission> submissions
    ) {
        Map<InterestField, AccuracyCount> accuracyCounts =
                new EnumMap<>(InterestField.class);

        for (InterestField category : InterestField.values()) {
            accuracyCounts.put(category, new AccuracyCount());
        }

        for (CompetitionProblemSubmission submission : submissions) {
            InterestField category = submission.getCompetitionProblem().getCategory();
            accuracyCounts.get(category).add(submission.isCorrect());
        }

        return Arrays.stream(InterestField.values())
                .map(category -> {
                    AccuracyCount count = accuracyCounts.get(category);
                    return new CategoryAccuracyResponseDto(
                            category,
                            count.solvedCount,
                            count.correctCount,
                            calculateRate(count.correctCount, count.solvedCount)
                    );
                })
                .toList();
    }

    private double calculateRate(long correctCount, long solvedCount) {
        if (solvedCount == 0) {
            return 0.0;
        }
        return (int) Math.round((correctCount * 100.0) / solvedCount);
    }

    // 잔디용 활동 수 세기
    private static class ActivityCount {
        private long competitionCount;
        private long interviewCount;

        private void increaseCompetition() {
            competitionCount++;
        }
        private void increaseInterview() {
            interviewCount++;
        }
    }

    // 분야별 정답률 계산
    private static class AccuracyCount {
        private long solvedCount;
        private long correctCount;

        private void add(boolean correct) {
            solvedCount++;
            if (correct) {
                correctCount++;
            }
        }
    }
}
