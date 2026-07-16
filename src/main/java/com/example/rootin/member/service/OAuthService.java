package com.example.rootin.member.service;

import com.example.rootin.global.jwt.JwtTokenProvider;
import com.example.rootin.member.dto.request.RefreshTokenRequestDto;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.member.repository.MemberRepository;
import com.example.rootin.member.dto.response.TokenResponseDto;
import com.example.rootin.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient = WebClient.create();

    @Value("${kakao.client-id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;

    @Transactional
    public TokenResponseDto kakaoLogin(String code) {
        String kakaoAccessToken = requestKakaoAccessToken(code);

        KakaoUserInfo kakaoUserInfo = requestKakaoUserInfo(kakaoAccessToken);

        String providerId = String.valueOf(kakaoUserInfo.id());

        String email = getEmail(kakaoUserInfo);
        String imageUrl = getProfileImage(kakaoUserInfo);

        MemberSearchResult searchResult =
                findOrCreateMember(
                        providerId,
                        email,
                        imageUrl
                );

        Member member = searchResult.member();

        String accessToken =
                jwtTokenProvider.createAccessToken(member);
        String refreshToken =
                jwtTokenProvider.createRefreshToken(member);

        member.updateRefreshToken(
                refreshToken,
                calculateRefreshTokenExpiresAt()
        );

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                searchResult.newMember(),
                MemberResponseDto.from(member)
        );

    }

    @Transactional
    public TokenResponseDto reissue(RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken을 입력해주세요.");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 refresh token입니다.");
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("회원 정보를 찾을 수 없습니다.")
                        );

        if (
                member.getRefreshToken() == null
                        || !member.getRefreshToken().equals(refreshToken)
        ) {
            throw new IllegalArgumentException("refresh token이 일치하지 않습니다.");
        }

        if (
                member.getRefreshTokenExpiresAt() == null
                        || member.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())
        ) {
            member.clearRefreshToken();
            throw new IllegalArgumentException("만료된 refresh token입니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(member);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member);

        member.updateRefreshToken(
                newRefreshToken,
                calculateRefreshTokenExpiresAt()
        );

        return new TokenResponseDto(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                false,
                MemberResponseDto.from(member)
        );
    }

    private String requestKakaoAccessToken( String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);
        body.add("redirect_uri", KAKAO_REDIRECT_URI);
        body.add("code", code);

        if (KAKAO_CLIENT_SECRET != null && !KAKAO_CLIENT_SECRET.isBlank()) {
            body.add( "client_secret", KAKAO_CLIENT_SECRET);
        }

        KakaoTokenResponse response =
                webClient.post()
                        .uri("https://kauth.kakao.com/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(body))
                        .retrieve()
                        .bodyToMono(KakaoTokenResponse.class)
                        .block();

        if (response == null || response.accessToken() == null
        ) {
            throw new IllegalArgumentException("카카오 Access Token 발급에 실패했습니다.");
        }

        return response.accessToken();
    }

    private KakaoUserInfo requestKakaoUserInfo(String accessToken) {
        KakaoUserInfo response =
                webClient.get()
                        .uri("https://kapi.kakao.com/v2/user/me")
                        .header("Authorization",
                                "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(KakaoUserInfo.class)
                        .block();

        if (response == null) {
            throw new IllegalArgumentException("카카오 사용자 정보를 조회하지 못했습니다.");
        }

        return response;
    }

    private MemberSearchResult findOrCreateMember(String providerId, String email, String imageUrl) {
        return memberRepository
                .findByProviderAndProviderId("KAKAO", providerId)
                .map(member -> {
                    member.updateOAuthInfo(email, imageUrl);

                    return new MemberSearchResult(member, false);
                })
                .orElseGet(() -> {
                    Member newMember = Member.createKakaoMember(providerId, email, imageUrl);

                    return new MemberSearchResult(memberRepository.save(newMember), true);
                });
    }

    private LocalDateTime calculateRefreshTokenExpiresAt() {
        return LocalDateTime.ofInstant(
                new Date(
                        System.currentTimeMillis()
                                + jwtTokenProvider.getRefreshExpiration()
                ).toInstant(),
                ZoneId.systemDefault()
        );
    }


    private String getEmail(KakaoUserInfo userInfo) {
        if (userInfo.kakaoAccount() == null) {
            return null;
        }

        return userInfo.kakaoAccount().email();
    }

    private String getProfileImage(KakaoUserInfo userInfo) {
        if (userInfo.kakaoAccount() == null || userInfo.kakaoAccount().profile() == null) {
            return null;
        }

        return userInfo
                .kakaoAccount()
                .profile()
                .profileImageUrl();
    }

    private record MemberSearchResult(
            Member member, boolean newMember
    ) { }


    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) { }

    private record KakaoUserInfo(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) { }

    private record KakaoAccount(
            String email, KakaoProfile profile
    ) { }

    private record KakaoProfile(
            @JsonProperty("profile_image_url") String profileImageUrl
    ) { }
}
