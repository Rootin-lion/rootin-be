package com.example.rootin.interview.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class InterviewTopicInsufficientException extends CustomException {

    public InterviewTopicInsufficientException() {
        super(ErrorCode.INTERVIEW_TOPIC_INSUFFICIENT);
    }
}
