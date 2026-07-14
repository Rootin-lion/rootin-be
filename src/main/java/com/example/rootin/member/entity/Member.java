package com.example.rootin.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Column(length = 100)
    private String email;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Column(length = 20)
    private String nickname;

    @Column(name = "age_group", length = 20)
    private String ageGroup;

    @Column(name = "interest_field", length = 300)
    private String interestField;

    @Column(nullable = false)
    private Long point = 0L;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 20)
    private String role = "ROLE_MEMBER";

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted = false;

    private Member(
            String provider,
            String providerId,
            String email,
            String imgUrl
    ) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.imgUrl = imgUrl;
        this.createdAt = LocalDateTime.now();
    }

    public static Member createKakaoMember(
            String providerId,
            String email,
            String imgUrl
    ) {
        return new Member(
                "KAKAO",
                providerId,
                email,
                imgUrl
        );
    }

    public void updateOAuthInfo(
            String email,
            String imgUrl
    ) {
        if (email != null && !email.isBlank()) {
            this.email = email;
        }

        if (imgUrl != null && !imgUrl.isBlank()) {
            this.imgUrl = imgUrl;
        }
    }

    public void completeProfile(
            String nickname,
            String ageGroup,
            List<String> interestFields
    ) {
        this.nickname = nickname;
        this.ageGroup = ageGroup;
        this.interestField = String.join(",", interestFields);
        this.profileCompleted = true;
    }
}