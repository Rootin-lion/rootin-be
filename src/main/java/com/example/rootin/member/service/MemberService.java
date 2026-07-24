package com.example.rootin.member.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.dto.request.ProfileRequestDto;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.member.dto.response.NicknameCheckResponseDto;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponseDto getProfile(Long memberId) {
        return MemberResponseDto.from(getMember(memberId));
    }

    @Transactional
    public MemberResponseDto completeProfile(
            Long memberId,
            ProfileRequestDto request
    ) {
        Member member = getMember(memberId);
        String nickname = normalizeNickname(request.getNickname());

        boolean nicknameChanged =
                member.getNickname() == null
                        || !nickname.equals(member.getNickname());

        if (nicknameChanged
                && memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        member.completeProfile(
                nickname,
                request.getAgeGroup(),
                request.getInterestFields()
        );

        return MemberResponseDto.from(member);
    }

    public NicknameCheckResponseDto checkNickname(String nickname) {
        String normalizedNickname = normalizeNickname(nickname);
        boolean duplicated =
                memberRepository.existsByNickname(normalizedNickname);

        if (duplicated) {
            return NicknameCheckResponseDto.duplicate();
        }

        return NicknameCheckResponseDto.available();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new CustomException(ErrorCode.NOT_FOUND)
                );
    }

    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        return nickname.trim();
    }
}