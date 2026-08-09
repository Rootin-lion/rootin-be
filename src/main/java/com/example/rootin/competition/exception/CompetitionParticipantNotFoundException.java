package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionParticipantNotFoundException extends CustomException {

    public CompetitionParticipantNotFoundException() {
        super(ErrorCode.COMPETITION_PARTICIPANT_NOT_FOUND);
    }
}