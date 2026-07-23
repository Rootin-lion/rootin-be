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

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    private static final Set<String> ALLOWED_INTERESTS =
            Set.of(
                    "DATABASE",
                    "INFRA_CLOUD",
                    "NETWORK",
                    "DATA_STRUCTURE_ALGORITHM",
                    "JAVA_SPRING",
                    "OPERATING_SYSTEM"
            );


    public MemberResponseDto getProfile(Long memberId) {
        return MemberResponseDto.from(
                getMember(memberId)
        );
    }

    @Transactional
    public MemberResponseDto completeProfile(
            Long memberId,
            ProfileRequestDto request) {
        Member member = getMember(memberId);
        String nickname = request.getNickname().trim();

        if (memberRepository.existsByNickname(nickname) && !nickname.equals(member.getNickname())) {
            throw new CustomException(ErrorCode.CONFLICT);
        }

        boolean invalidInterest =
                request.getInterestFields()
                        .stream()
                        .anyMatch(interest -> !ALLOWED_INTERESTS.contains(interest));

        if (invalidInterest) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        member.completeProfile(
                nickname,
                request.getAgeGroup(),
                request.getInterestFields()
        );

        return MemberResponseDto.from(member);
    }

    private Member getMember(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    //닉네임 중복 확인
    public NicknameCheckResponseDto checkNickname(String nickname) {
        String normalizedNickname = normalizeNickname(nickname);
        boolean duplicated = memberRepository.existsByNickname(normalizedNickname);

        if(duplicated) {
            return NicknameCheckResponseDto.duplicate();
        }
        return NicknameCheckResponseDto.available();
    }
    //닉네임 미입력 경우 처리, 공백 제거
    private String normalizeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        return nickname.trim();
    }
}
