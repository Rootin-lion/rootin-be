package com.example.rootin.mypage.dashboard.service;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.bookmark.repository.CompetitionProblemBookmarkRepository;
import com.example.rootin.competition.domain.CompetitionParticipant;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.domain.CompetitionProblemSubmission;
import com.example.rootin.competition.repository.CompetitionParticipantRepository;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.competition.repository.CompetitionProblemSubmissionRepository;
import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.interview.repository.InterviewRepository;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.domain.InterestField;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import com.example.rootin.mypage.dashboard.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    private static final int RECENT_PROBLEM_LIMIT = 3; // 대시보드에 보여줄 문항 수

    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionProblemRepository competitionProblemRepository;
    private final CompetitionProblemSubmissionRepository competitionProblemSubmissionRepository;
    private final CompetitionProblemBookmarkRepository competitionProblemBookmarkRepository;
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
                createCategoryAccuracies(completedCompetitions, submissions),
                getRecentBookmarks(memberId),
                getRecentWrongAnswers(memberId)
        );
    }

    private List<BookmarkResponseDto> getRecentBookmarks(Long memberId) {
        return competitionProblemBookmarkRepository
                .findRecentByMemberId(memberId, PageRequest.of(0, RECENT_PROBLEM_LIMIT))
                .stream()
                .map(CompetitionProblemBookmark::getProblem)
                .map(problem -> new BookmarkResponseDto(
                        problem.getId(),
                        problem.getCategory(),
                        problem.getTitle()
                ))
                .toList();
    }

    private List<WrongAnswerResponseDto> getRecentWrongAnswers(Long memberId) {
        return competitionProblemSubmissionRepository
                .findRecentWrongSubmissionsByMemberId(memberId, PageRequest.of(0, RECENT_PROBLEM_LIMIT))
                .stream()
                .map(submission -> {
                    var competitionProblem = submission.getCompetitionProblem();
                    var problem = competitionProblem.getProblem();
                    return new WrongAnswerResponseDto(
                            competitionProblem.getCompetition().getId(),
                            competitionProblem.getId(),
                            problem.getCategory(),
                            problem.getTitle()
                    );
                })
                .toList();
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
                .map(entry -> new DailyActivityResponseDto(
                        entry.getKey(),
                        entry.getValue().competitionCount + entry.getValue().interviewCount
                ))
                .toList();
    }

    private List<CategoryAccuracyResponseDto> createCategoryAccuracies(
            List<CompetitionParticipant> completedCompetitions, //사용자가 최종 제출한 대회 목록
            List<CompetitionProblemSubmission> submissions // 해당 대회에서 사용자가 실제로 답안을 선택한 문제 목록
    ) {
        Map<InterestField, AccuracyCount> accuracyCounts =
                new EnumMap<>(InterestField.class);

        for (InterestField category : InterestField.values()) {
            accuracyCounts.put(category, new AccuracyCount());
        }

        if (!completedCompetitions.isEmpty()) { // 완료한 대회에 출제된 모든 문제 조회
            List<CompetitionProblem> competitionProblems = competitionProblemRepository.findByCompetitionIn(
                    completedCompetitions.stream()
                            .map(CompetitionParticipant::getCompetition)
                            .toList()
            );
            for (CompetitionProblem competitionProblem : competitionProblems) { // 각 문제의 분야 확인 후 카운트 증가
                InterestField category = competitionProblem.getProblem().getCategory();
                accuracyCounts.get(category).increaseTotalCount();
            }
        }

        for (CompetitionProblemSubmission submission : submissions) {
            if (submission.isCorrect()) {
                InterestField category = submission.getCompetitionProblem().getProblem().getCategory();
                accuracyCounts.get(category).increaseCorrectCount(); // 제출 답안 중 분야별 정답만 카운트 증가
            }
        }

        return Arrays.stream(InterestField.values())
                .map(category -> {
                    AccuracyCount count = accuracyCounts.get(category);
                    return new CategoryAccuracyResponseDto(
                            category,
                            calculateRate(count.correctCount, count.totalCount)
                    );
                })
                .toList();
    }

    private int calculateRate(long correctCount, long totalCount) {
        if (totalCount == 0) {
            return 0;
        }
        return (int) Math.round((correctCount * 100.0) / totalCount);
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
        private long totalCount;
        private long correctCount;

        private void increaseTotalCount() {
            totalCount++;
        }

        private void increaseCorrectCount() {
            correctCount++;
        }
    }
}
