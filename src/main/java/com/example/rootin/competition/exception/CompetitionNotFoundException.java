package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionNotFoundException extends CustomException {

    public CompetitionNotFoundException() {
        super(ErrorCode.COMPETITION_NOT_FOUND);
    }
}