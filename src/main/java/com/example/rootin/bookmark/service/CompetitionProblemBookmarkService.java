package com.example.rootin.bookmark.service;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.bookmark.exception.ProblemBookmarkAlreadyExistsException;
import com.example.rootin.bookmark.exception.ProblemBookmarkNotFoundException;
import com.example.rootin.bookmark.repository.CompetitionProblemBookmarkRepository;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.exception.CompetitionProblemNotFoundException;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompetitionProblemBookmarkService {

    private final CompetitionProblemRepository competitionProblemRepository;
    private final CompetitionProblemBookmarkRepository competitionProblemBookmarkRepository;

    @Transactional
    public void addBookmark(Long problemId, Long memberId) {
        CompetitionProblem problem = competitionProblemRepository.findById(problemId)
                .orElseThrow(CompetitionProblemNotFoundException::new);

        if (competitionProblemBookmarkRepository.findByMemberIdAndCompetitionProblem(memberId, problem).isPresent()) {
            throw new ProblemBookmarkAlreadyExistsException();
        }

        competitionProblemBookmarkRepository.save(
                new CompetitionProblemBookmark(memberId, problem, LocalDateTime.now()));
    }

    @Transactional
    public void removeBookmark(Long problemId, Long memberId) {
        CompetitionProblem problem = competitionProblemRepository.findById(problemId)
                .orElseThrow(CompetitionProblemNotFoundException::new);

        CompetitionProblemBookmark bookmark = competitionProblemBookmarkRepository
                .findByMemberIdAndCompetitionProblem(memberId, problem)
                .orElseThrow(ProblemBookmarkNotFoundException::new);

        competitionProblemBookmarkRepository.delete(bookmark);
    }
}