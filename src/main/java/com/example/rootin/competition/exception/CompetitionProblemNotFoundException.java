package com.example.rootin.competition.exception;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;

public class CompetitionProblemNotFoundException extends CustomException {

    public CompetitionProblemNotFoundException() {
        super(ErrorCode.COMPETITION_PROBLEM_NOT_FOUND);
    }
}
