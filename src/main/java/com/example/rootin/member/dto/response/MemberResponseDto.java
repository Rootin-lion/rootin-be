package com.example.rootin.member.dto.response;

import com.example.rootin.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {

    private Long id;
    private String provider;
    private String email;
    private String imgUrl;
    private String nickname;
    private String ageGroup;
    private List<String> interestFields;
    private Long point;
    private Integer streakDays;
    private String role;
    private boolean profileCompleted;

    public static MemberResponseDto from(Member member) {
        List<String> interests =
                member.getInterestField() == null
                        || member.getInterestField().isBlank()
                        ? List.of()
                        : Arrays.asList(
                        member.getInterestField().split(",")
                );

        return new MemberResponseDto(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getImgUrl(),
                member.getNickname(),
                member.getAgeGroup(),
                interests,
                member.getPoint(),
                member.getStreakDays(),
                member.getRole(),
                member.isProfileCompleted()
        );
    }
}
