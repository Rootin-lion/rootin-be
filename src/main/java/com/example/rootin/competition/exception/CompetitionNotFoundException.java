package com.example.rootin.competition.exception;

import java.time.LocalDate;

public class CompetitionNotFoundException extends RuntimeException {
    public CompetitionNotFoundException(LocalDate competitionDate) {
        super("해당 날짜에 진행되는 대회가 없습니다. date=" + competitionDate);
    }
}
