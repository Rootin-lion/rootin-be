package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionAlreadySubmittedException extends CustomException {

    public CompetitionAlreadySubmittedException() {
        super(ErrorCode.COMPETITION_ALREADY_SUBMITTED);
    }
}

