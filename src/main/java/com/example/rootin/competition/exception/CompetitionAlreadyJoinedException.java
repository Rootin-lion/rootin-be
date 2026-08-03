package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionAlreadyJoinedException extends CustomException {

    public CompetitionAlreadyJoinedException() {
        super(ErrorCode.COMPETITION_ALREADY_JOINED);
    }
}