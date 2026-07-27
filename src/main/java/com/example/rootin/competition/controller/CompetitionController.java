package com.example.rootin.competition.controller;

import com.example.rootin.competition.dto.response.CompetitionTodayResponse;
import com.example.rootin.competition.service.CompetitionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    // TODO: 공통 응답 래퍼 수정
    @GetMapping("/today")
    @Operation(summary = "현재 진행중인 대회 조회")
    public CompetitionTodayResponse getTodayCompetition() {
        return competitionService.getTodayCompetition();
    }
}