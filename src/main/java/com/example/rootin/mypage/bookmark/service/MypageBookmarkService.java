package com.example.rootin.mypage.bookmark.service;

import com.example.rootin.bookmark.domain.CompetitionProblemBookmark;
import com.example.rootin.bookmark.repository.CompetitionProblemBookmarkRepository;
import com.example.rootin.competition.domain.CompetitionProblem;
import com.example.rootin.competition.repository.CompetitionProblemRepository;
import com.example.rootin.global.common.PageResponse;
import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.mypage.bookmark.dto.response.MypageBookmarkResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageBookmarkService {
    private static final int BOOKMARK_PAGE_SIZE = 4;

    private final CompetitionProblemBookmarkRepository competitionProblemBookmarkRepository;
    private final CompetitionProblemRepository competitionProblemRepository;

    public PageResponse<MypageBookmarkResponseDto> getBookmarks(Long memberId, int page) {
        if (page < 1) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        Page<CompetitionProblemBookmark> bookmarks = competitionProblemBookmarkRepository
                .findByMemberIdOrderByCreatedAtDescIdDesc(
                        memberId,
                        PageRequest.of(page - 1, BOOKMARK_PAGE_SIZE)
                );

        var problemIds = bookmarks.getContent().stream()
                .map(bookmark -> bookmark.getProblem().getId())
                .toList();

        Map<Long, CompetitionProblem> latestCompetitionProblemByProblemId = problemIds.isEmpty()
                ? Map.of()
                : competitionProblemRepository
                        .findCompletedByMemberIdAndProblemIds(memberId, problemIds)
                        .stream()
                        .collect(Collectors.toMap( //동일 문제가 여러 대회에 출제됐을 경우 가장 최근 대회 정보 사용
                                competitionProblem -> competitionProblem.getProblem().getId(),
                                Function.identity(),
                                (latest, ignored) -> latest,
                                LinkedHashMap::new
                        ));

        Page<MypageBookmarkResponseDto> responsePage = bookmarks.map(bookmark -> {
            CompetitionProblem competitionProblem = latestCompetitionProblemByProblemId
                    .get(bookmark.getProblem().getId());
            if (competitionProblem == null) {
                throw new CustomException(ErrorCode.NOT_FOUND);
            }
            return MypageBookmarkResponseDto.from(competitionProblem);
        });

        return PageResponse.of(responsePage);
    }
}
