package com.example.rootin.interview.service;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.InterviewTopic;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterviewPromptFactory {

    public String buildBasicQuestionPrompt(Interview interview, InterviewTopic topic, List<String> usedQuestions) {
        return """
                당신은 IT 기술 면접관이다. 아래 정보를 바탕으로 기본 질문 하나를 만들고 JSON으로만 응답하라.

                규칙:
                - questionText는 자연스러운 한국어 질문 한 문장이다.
                - targetKeywords는 이 질문이 실제로 답변을 요구하는 개념만 고른다.
                - topicKeywords 전체를 무조건 복사하지 않는다.
                - targetKeywords의 값은 반드시 제공된 topicKeywords 안에서만 고른다.
                - 이전 질문과 동일하거나 같은 목적의 질문을 반복하지 않는다.

                category: %s
                topicName: %s
                topicKeywords: %s
                interviewMode: %s
                previousQuestions: %s
                """.formatted(interview.getField(), topic.getTopicName(), topic.getKeywords(),
                interview.getInterviewMode(), usedQuestions);
    }

    public String buildFollowUpQuestionPrompt(
            Interview interview,
            InterviewTopic topic,
            InterviewQuestion currentQuestion,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            String previousFeedback,
            List<String> usedQuestions
    ) {
        return """
                당신은 IT 기술 면접관이다. 부족하게 설명된 개념만 확인하는 꼬리질문 하나를 만들고 JSON으로만 응답하라.

                규칙:
                - targetKeywords는 missingKeywords의 일부 또는 전체여야 한다.
                - 이미 충족한 개념은 다시 묻지 않는다.
                - 현재 질문 전체를 표현만 바꾸어 반복하지 않는다.
                - questionText가 요구하는 개념과 targetKeywords가 정확히 일치해야 한다.

                category: %s
                topicName: %s
                currentQuestion: %s
                matchedKeywords: %s
                missingKeywords: %s
                previousFeedback: %s
                interviewMode: %s
                previousQuestions: %s
                """.formatted(interview.getField(), topic.getTopicName(), currentQuestion.getQuestionText(),
                matchedKeywords, missingKeywords, previousFeedback, interview.getInterviewMode(), usedQuestions);
    }

    public String buildEvaluationPrompt(InterviewQuestion question, String answerText, List<String> targetKeywords) {
        return """
                당신은 기술 면접 답변 평가자다. 질문과 답변의 의미를 평가하고 JSON으로만 응답하라.

                규칙:
                - 단어 포함 여부가 아니라 개념을 정확히 설명했는지 판단한다.
                - 용어를 언급만 하거나 동어 반복한 경우 matchedKeywords에 넣지 않는다.
                - 정확한 용어 없이 같은 의미를 올바르게 설명한 경우에는 해당 개념을 matchedKeywords에 넣는다.
                - matchedKeywords는 targetKeywords 목록의 값만 사용한다.
                - score는 의미적 정확성과 완성도를 기준으로 0~100 정수로 평가한다.
                - evaluation은 CORRECT, PARTIAL, INCORRECT, UNKNOWN 중 하나다.
                - 모른다는 답변은 UNKNOWN, 명백히 틀린 답변은 INCORRECT로 평가한다.
                - feedback은 구체적이고 간결하게 작성한다.

                question: %s
                targetKeywords: %s
                answer: %s
                """.formatted(question.getQuestionText(), targetKeywords, answerText);
    }
}
