package com.example.rootin.interview.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.AnswerEvaluation;
import com.example.rootin.interview.domain.InterviewAnswer;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewQuestionType;
import com.example.rootin.interview.domain.InterviewSessionTopic;
import com.example.rootin.interview.domain.InterviewStatus;
import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.interview.dto.request.InterviewCreateRequestDto;
import com.example.rootin.interview.dto.response.InterviewQuestionDto;
import com.example.rootin.interview.dto.response.InterviewQuestionReportDto;
import com.example.rootin.interview.dto.response.InterviewReportResponseDto;
import com.example.rootin.interview.dto.response.InterviewSessionResponseDto;
import com.example.rootin.interview.dto.response.InterviewTopicReportDto;
import com.example.rootin.interview.service.result.GeneratedQuestionResult;
import com.example.rootin.interview.service.result.InterviewAnswerResult;
import com.example.rootin.interview.service.result.InterviewQuestionResult;
import com.example.rootin.interview.repository.InterviewAnswerRepository;
import com.example.rootin.interview.repository.InterviewQuestionRepository;
import com.example.rootin.interview.repository.InterviewRepository;
import com.example.rootin.interview.repository.InterviewSessionTopicRepository;
import com.example.rootin.interview.repository.InterviewTopicRepository;
import com.example.rootin.member.domain.InterestField;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int MAX_FOLLOW_UP_DEPTH = 1;

    private final InterviewRepository interviewRepository;
    private final InterviewTopicRepository interviewTopicRepository;
    private final InterviewSessionTopicRepository interviewSessionTopicRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionGenerationService interviewQuestionGenerationService;

    @Transactional
    public InterviewSessionResponseDto createSession(Long memberId, InterviewCreateRequestDto request) {
        List<InterviewTopic> selectedTopics = selectTopics(request.getField(), request.getQuestionCount());

        Interview interview = Interview.create(
                memberId,
                request.getField(),
                request.getQuestionCount(),
                request.getInterviewMode()
        );
        Interview savedInterview = interviewRepository.save(interview);

        List<InterviewSessionTopic> sessionTopics = new ArrayList<>();
        for (int i = 0; i < selectedTopics.size(); i++) {
            sessionTopics.add(interviewSessionTopicRepository.save(
                    InterviewSessionTopic.create(savedInterview, selectedTopics.get(i), i + 1)
            ));
        }
        //첫 번째 주제에서의 질문 생성(기본질문)
        InterviewSessionTopic firstSessionTopic = sessionTopics.get(0);
        GeneratedQuestionResult generatedQuestion =
                interviewQuestionGenerationService.generateBasicQuestion(savedInterview, firstSessionTopic);
        //질문저장
        InterviewQuestion savedQuestion = interviewQuestionRepository.save(
                InterviewQuestion.create(
                        savedInterview,
                        firstSessionTopic,
                        firstSessionTopic.getInterviewTopic(),
                        InterviewQuestionType.BASIC,
                        1,
                        generatedQuestion.questionText(),
                        generatedQuestion.targetKeywords()
                )
        );
        firstSessionTopic.incrementBasicQuestionCount();

        return new InterviewSessionResponseDto(
                savedInterview.getId(),
                savedInterview.getField(),
                savedInterview.getQuestionCount(),
                savedInterview.getInterviewMode(),
                savedInterview.getStatus(),
                savedInterview.getStartedAt(),
                List.of(toQuestionDto(savedQuestion))
        );
    }
    //사용자 질문 답변
    @Transactional
    public InterviewAnswerResult submitAnswer(Long memberId, Long interviewId, Long questionId, String answerText) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!Objects.equals(interview.getMemberId(), memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        //어떤 질문에 답변했는가
        InterviewQuestion currentQuestion = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        validateQuestionBelongsToInterview(interviewId, currentQuestion);
        //같은 질문에 중복 답변 방지
        if (interviewAnswerRepository.findByInterviewQuestion_Id(currentQuestion.getId()).isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT);
        }
        //답변 평가
        InterviewQuestionGenerationService.AnswerEvaluationResult evaluation =
                interviewQuestionGenerationService.evaluateAnswer(currentQuestion, answerText);

        InterviewAnswer savedAnswer = interviewAnswerRepository.save(
                InterviewAnswer.create(
                        currentQuestion,
                        answerText,
                        evaluation.matchedKeywords(),
                        evaluation.missingKeywords(),
                        evaluation.keywordCoverage(),
                        evaluation.score(),
                        evaluation.evaluation(),
                        evaluation.feedback()
                )
        );
        //꼬리 질문 여부 판단
        InterviewSessionTopic sessionTopic = currentQuestion.getSessionTopic();
        boolean shouldAskFollowUp = shouldAskFollowUp(currentQuestion, sessionTopic, evaluation);
        InterviewQuestionResult nextQuestion = null;

        if (shouldAskFollowUp) {
            GeneratedQuestionResult generatedFollowUp = interviewQuestionGenerationService.generateFollowUpQuestion(
                    interview,
                    sessionTopic,
                    currentQuestion,
                    evaluation.matchedKeywords(),
                    evaluation.missingKeywords(),
                    evaluation.feedback()
            );

            InterviewQuestion savedFollowUp = interviewQuestionRepository.save(
                    InterviewQuestion.create(
                            interview,
                            sessionTopic,
                            sessionTopic.getInterviewTopic(),
                            InterviewQuestionType.FOLLOW_UP,
                            nextQuestionOrder(interviewId),
                            generatedFollowUp.questionText(),
                            generatedFollowUp.targetKeywords()
                    )
            );
            sessionTopic.incrementFollowUpQuestionCount();
            nextQuestion = toQuestionResult(savedFollowUp, null);
        } else {
            sessionTopic.complete();
            nextQuestion = openNextTopicOrFinish(interview);
        }

        if (nextQuestion == null && interview.getStatus() == InterviewStatus.COMPLETED) {
            return new InterviewAnswerResult(
                    interview.getId(),
                    savedAnswer.getId(),
                    true,
                    true,
                    toQuestionResult(currentQuestion, savedAnswer),
                    null,
                    buildReport(interview)
            );
        }

        return new InterviewAnswerResult(
                interview.getId(),
                savedAnswer.getId(),
                sessionTopic.isCompleted(),
                interview.getStatus() == InterviewStatus.COMPLETED,
                toQuestionResult(currentQuestion, savedAnswer),
                nextQuestion,
                interview.getStatus() == InterviewStatus.COMPLETED ? buildReport(interview) : null
        );
    }

    @Transactional
    public InterviewReportResponseDto getReport(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return buildReport(interview);
    }

    private List<InterviewTopic> selectTopics(InterestField field, int questionCount) {
        List<InterviewTopic> topics = interviewTopicRepository
                .findByCategoryOrderByIdAsc(field, PageRequest.of(0, questionCount, Sort.by("id").ascending()))
                .getContent();

        if (topics.size() < questionCount) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        return topics;
    }

    private void validateQuestionBelongsToInterview(Long interviewId, InterviewQuestion question) {
        if (!Objects.equals(question.getInterview().getId(), interviewId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    boolean shouldAskFollowUp(
            InterviewQuestion currentQuestion,
            InterviewSessionTopic sessionTopic,
            InterviewQuestionGenerationService.AnswerEvaluationResult evaluation
    ) {
        return currentQuestion.getQuestionType() == InterviewQuestionType.BASIC
                && evaluation.evaluation() == AnswerEvaluation.PARTIAL
                && !evaluation.missingKeywords().isEmpty()
                && sessionTopic.getFollowUpQuestionCount() < MAX_FOLLOW_UP_DEPTH;
    }

    private InterviewQuestionResult openNextTopicOrFinish(Interview interview) {
        InterviewSessionTopic nextTopic = interviewSessionTopicRepository
                .findFirstByInterviewIdAndCompletedFalseOrderByTopicOrderAsc(interview.getId())
                .orElse(null);

        if (nextTopic == null) {
            interview.updateStatus(InterviewStatus.COMPLETED);
            interviewRepository.save(interview);
            return null;
        }

        GeneratedQuestionResult generatedQuestion = interviewQuestionGenerationService.generateBasicQuestion(
                interview,
                nextTopic
        );

        InterviewQuestion savedQuestion = interviewQuestionRepository.save(
                InterviewQuestion.create(
                        interview,
                        nextTopic,
                        nextTopic.getInterviewTopic(),
                        InterviewQuestionType.BASIC,
                        nextQuestionOrder(interview.getId()),
                        generatedQuestion.questionText(),
                        generatedQuestion.targetKeywords()
                )
        );
        nextTopic.incrementBasicQuestionCount();
        return toQuestionResult(savedQuestion, null);
    }

    private int nextQuestionOrder(Long interviewId) {
        return (int) interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interviewId).size() + 1;
    }

    private InterviewQuestionResult toQuestionResult(
            InterviewQuestion question,
            InterviewAnswer answer
    ) {
        return new InterviewQuestionResult(
                question.getId(),
                question.getInterviewTopic().getId(),
                question.getInterviewTopic().getTopicName(),
                question.getSessionTopic().getTopicOrder(),
                question.getQuestionOrder(),
                question.getQuestionType(),
                question.getQuestionText(),
                question.getTargetKeywords(),
                answer == null ? null : answer.getMatchedKeywords(),
                answer == null ? null : answer.getMissingKeywords(),
                answer == null ? null : answer.getKeywordCoverage(),
                answer == null ? null : answer.getScore(),
                answer == null ? null : answer.getEvaluation(),
                answer == null ? null : answer.getAnswerText(),
                answer == null ? null : answer.getFeedback()
        );
    }

    private InterviewQuestionDto toQuestionDto(InterviewQuestion question) {
        return new InterviewQuestionDto(
                question.getId(),
                question.getInterviewTopic().getId(),
                question.getInterviewTopic().getTopicName(),
                question.getSessionTopic().getTopicOrder(),
                question.getQuestionOrder(),
                question.getQuestionType(),
                question.getQuestionText(),
                question.getTargetKeywords()
        );
    }
    //리포트생성
    private InterviewReportResponseDto buildReport(Interview interview) {
        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interview.getId());
        List<InterviewSessionTopic> sessionTopics = interviewSessionTopicRepository.findByInterviewIdOrderByTopicOrderAsc(interview.getId());
        Map<Long, InterviewAnswer> answerByQuestionId = interviewAnswerRepository
                .findAllByInterviewIdOrderByQuestionOrder(interview.getId())
                .stream()
                .collect(Collectors.toMap(
                        answer -> answer.getInterviewQuestion().getId(),
                        Function.identity(),
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ));

        Map<Long, List<InterviewQuestion>> questionByTopic = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getInterviewTopic().getId(), Collectors.toList()));

        List<InterviewTopicReportDto> topicReports = sessionTopics.stream()
                .map(topic -> buildTopicReport(
                        topic,
                        questionByTopic.getOrDefault(topic.getInterviewTopic().getId(), List.of()),
                        answerByQuestionId
                ))
                .toList();

        double overallCoverage = averageDoubles(topicReports.stream().map(InterviewTopicReportDto::keywordCoverage).toList());
        List<InterviewQuestionReportDto> questionReports = questions.stream()
                .map(question -> toQuestionReport(question, answerByQuestionId.get(question.getId())))
                .toList();
        double averageScore = questionReports.stream()
                .map(InterviewQuestionReportDto::score)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        List<String> learningGuide = buildLearningGuide(topicReports);
        String summary = "Completed %d topics with %.1f%% average keyword coverage.".formatted(topicReports.size(), overallCoverage);

        return new InterviewReportResponseDto(
                interview.getId(),
                round(overallCoverage),
                topicReports,
                learningGuide,
                summary,
                round(averageScore),
                questionReports
        );
    }

    private InterviewQuestionReportDto toQuestionReport(InterviewQuestion question, InterviewAnswer answer) {
        return new InterviewQuestionReportDto(
                question.getId(),
                question.getInterviewTopic().getId(),
                question.getInterviewTopic().getTopicName(),
                question.getQuestionOrder(),
                question.getQuestionType(),
                question.getQuestionText(),
                answer == null ? null : answer.getAnswerText(),
                answer == null ? null : answer.getFeedback(),
                question.getTargetKeywords(),
                answer == null ? List.of() : answer.getMatchedKeywords(),
                answer == null ? question.getTargetKeywords() : answer.getMissingKeywords(),
                answer == null ? null : answer.getKeywordCoverage(),
                answer == null ? null : answer.getScore(),
                answer == null ? null : answer.getEvaluation()
        );
    }
    //주제별 점수 계산
    private InterviewTopicReportDto buildTopicReport(
            InterviewSessionTopic topic,
            List<InterviewQuestion> questions,
            Map<Long, InterviewAnswer> answerByQuestionId
    ) {
        Set<String> targetKeywords = questions.stream()
                .flatMap(question -> question.getTargetKeywords() == null ? java.util.stream.Stream.empty() : question.getTargetKeywords().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> matchedKeywords = questions.stream()
                .map(InterviewQuestion::getId)
                .map(answerByQuestionId::get)
                .filter(Objects::nonNull)
                .flatMap(answer -> answer.getMatchedKeywords() == null ? java.util.stream.Stream.empty() : answer.getMatchedKeywords().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> missingKeywords = new LinkedHashSet<>(targetKeywords);
        missingKeywords.removeAll(matchedKeywords);
        //키워드 기반 점수 계산
        double coverage = targetKeywords.isEmpty() ? 100.0 : (matchedKeywords.size() * 100.0) / targetKeywords.size();

        return new InterviewTopicReportDto(
                topic.getInterviewTopic().getId(),
                topic.getInterviewTopic().getTopicName(),
                topic.getTopicOrder(),
                round(coverage),
                new ArrayList<>(matchedKeywords),
                new ArrayList<>(missingKeywords)
//                (int) questions.stream().filter(q -> q.getQuestionType() == InterviewQuestionType.BASIC).count(),
//                (int) questions.stream().filter(q -> q.getQuestionType() == InterviewQuestionType.FOLLOW_UP).count()
        );
    }

    private List<String> buildLearningGuide(List<InterviewTopicReportDto> topicReports) {
        return topicReports.stream()
                .flatMap(report -> report.missingKeywords().stream())
                .distinct()
                .toList();
    }

    private double averageDoubles(List<Double> values) {
        List<Double> filtered = values.stream().filter(Objects::nonNull).toList();
        if (filtered.isEmpty()) {
            return 0.0;
        }
        return filtered.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
