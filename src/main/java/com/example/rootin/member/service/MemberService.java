package com.example.rootin.member.service;

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
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        boolean invalidInterest =
                request.getInterestFields()
                        .stream()
                        .anyMatch(interest -> !ALLOWED_INTERESTS.contains(interest));

        if (invalidInterest) {
            throw new IllegalArgumentException("올바르지 않은 관심 분야가 포함되어 있습니다.");
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
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
                );
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
        if(nickname == null || nickname.isBlank()){
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        return nickname.trim();
    }
}
