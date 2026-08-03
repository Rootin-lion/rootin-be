package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionNotJoinableException extends CustomException {

    public CompetitionNotJoinableException() {
        super(ErrorCode.COMPETITION_NOT_JOINABLE);
    }
}

