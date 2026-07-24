package com.example.rootin.member.entity;

import com.example.rootin.member.domain.InterestField;
import jakarta.persistence.*;
import com.example.rootin.member.domain.MemberRole;
import com.example.rootin.member.domain.OAuthProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ElementCollection
    @CollectionTable(
            name = "member_interest_field",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_field")
    private List<InterestField> interestFields = new ArrayList<>();

    @Column(nullable = false)
    private Long point = 0L;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;


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
        this.role = MemberRole.ROLE_MEMBER;
    }

    public static Member createKakaoMember(
            String providerId,
            String email,
            String imgUrl
    ) {
        return createOAuthMember(OAuthProvider.KAKAO, providerId, email, imgUrl);
    }

    public static Member createGoogleMember(
            String providerId,
            String email,
            String imgUrl
    ) {
        return createOAuthMember(OAuthProvider.GOOGLE, providerId, email, imgUrl);
    }

    public static Member createOAuthMember(
            String provider,
            String providerId,
            String email,
            String imgUrl
    ) {
        return new Member(provider, providerId, email, imgUrl);
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
            List<InterestField> interestFields
    ) {
        this.nickname = nickname;
        this.ageGroup = ageGroup;

        this.interestFields.clear();

        if (interestFields != null) {
            this.interestFields.addAll(interestFields);
        }
    }

}
