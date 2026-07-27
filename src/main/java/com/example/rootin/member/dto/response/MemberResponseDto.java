package com.example.rootin.member.dto.response;

import com.example.rootin.member.domain.InterestField;
import com.example.rootin.member.domain.MemberRole;
import com.example.rootin.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private List<InterestField> interestFields;
    private Long point;
    private Integer streakDays;
    private MemberRole role;

    public static MemberResponseDto from(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getProvider(),
                member.getEmail(),
                member.getImgUrl(),
                member.getNickname(),
                member.getAgeGroup(),
                List.copyOf(member.getInterestFields()),
                member.getPoint(),
                member.getStreakDays(),
                member.getRole()
        );
    }
}