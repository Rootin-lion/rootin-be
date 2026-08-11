package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionNotSubmittedException extends CustomException {

    public CompetitionNotSubmittedException() {
        super(ErrorCode.COMPETITION_NOT_SUBMITTED);
    }
}
