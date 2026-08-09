package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionOptionNotFoundException extends CustomException {

    public CompetitionOptionNotFoundException() {
        super(ErrorCode.COMPETITION_OPTION_NOT_FOUND);
    }
}
