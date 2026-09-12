package com.example.rootin.interview.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class InterviewMemberNotFoundException extends CustomException {

    public InterviewMemberNotFoundException() {
        super(ErrorCode.INTERVIEW_MEMBER_NOT_FOUND);
    }
}
