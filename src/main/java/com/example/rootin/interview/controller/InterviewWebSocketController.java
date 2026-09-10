package com.example.rootin.interview.controller;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.interview.domain.InterviewQuestionType;
import com.example.rootin.interview.dto.socket.InterviewAnswerMessage;
import com.example.rootin.interview.dto.socket.InterviewCompletedPayload;
import com.example.rootin.interview.dto.socket.InterviewErrorPayload;
import com.example.rootin.interview.dto.socket.InterviewEventType;
import com.example.rootin.interview.dto.socket.InterviewFeedbackPayload;
import com.example.rootin.interview.dto.socket.InterviewQuestionPayload;
import com.example.rootin.interview.dto.socket.InterviewSocketMessage;
import com.example.rootin.interview.service.InterviewService;
import com.example.rootin.interview.service.result.InterviewAnswerResult;
import com.example.rootin.interview.service.result.InterviewQuestionResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class InterviewWebSocketController {

    private final InterviewService interviewService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/interviews/{interviewId}/answers")
    public void submitAnswer(
            Principal principal,
            @DestinationVariable Long interviewId,
            @Valid @Payload InterviewAnswerMessage request
    ) {
        Long memberId = Long.valueOf(principal.getName());
        InterviewAnswerResult result = interviewService.submitAnswer(
                memberId,
                interviewId,
                request.questionId(),
                request.answerText()
        );

        String destination = "/queue/interviews/" + interviewId;
        InterviewQuestionResult evaluated = result.evaluatedQuestion();
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                destination,
                InterviewSocketMessage.of(
                        InterviewEventType.FEEDBACK,
                        interviewId,
                        request.clientMessageId(),
                        new InterviewFeedbackPayload(
                                evaluated.questionId(),
                                result.answerId(),
                                evaluated.answer(),
                                evaluated.feedback(),
                                evaluated.matchedKeywords(),
                                evaluated.missingKeywords(),
                                evaluated.keywordCoverage(),
                                result.topicCompleted()
                        )
                )
        );

        if (result.interviewCompleted()) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    destination,
                    InterviewSocketMessage.of(
                            InterviewEventType.INTERVIEW_COMPLETED,
                            interviewId,
                            request.clientMessageId(),
                            new InterviewCompletedPayload(result.report())
                    )
            );
            return;
        }

        InterviewQuestionResult next = result.nextQuestion();
        Long parentQuestionId = next.questionType() == InterviewQuestionType.FOLLOW_UP
                ? evaluated.questionId()
                : null;
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                destination,
                InterviewSocketMessage.of(
                        InterviewEventType.QUESTION,
                        interviewId,
                        request.clientMessageId(),
                        new InterviewQuestionPayload(
                                next.questionId(),
                                next.topicId(),
                                next.topicName(),
                                next.topicOrder(),
                                next.questionOrder(),
                                next.questionType(),
                                next.question(),
                                parentQuestionId
                        )
                )
        );
    }

    @MessageExceptionHandler(CustomException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public InterviewSocketMessage<InterviewErrorPayload> handleCustomException(CustomException exception) {
        ErrorCode error = exception.getErrorCode();
        return InterviewSocketMessage.of(
                InterviewEventType.ERROR,
                null,
                null,
                new InterviewErrorPayload(error.getCode(), error.getMessage())
        );
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public InterviewSocketMessage<InterviewErrorPayload> handleException(Exception exception) {
        ErrorCode error = ErrorCode.BAD_REQUEST;
        return InterviewSocketMessage.of(
                InterviewEventType.ERROR,
                null,
                null,
                new InterviewErrorPayload(error.getCode(), exception.getMessage())
        );
    }
}
