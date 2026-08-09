package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionTimeExpiredException extends CustomException {

    public CompetitionTimeExpiredException() {
        super(ErrorCode.COMPETITION_TIME_EXPIRED);
    }
}