package com.example.rootin.member.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.global.jwt.JwtTokenProvider;
import com.example.rootin.member.domain.OAuthProvider;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.member.dto.response.TokenResponseDto;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient = WebClient.create();

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Transactional
    public TokenIssueResult kakaoLogin(String code) {
        String accessToken = requestKakaoAccessToken(normalizeCode(code));
        KakaoUserInfo userInfo = requestKakaoUserInfo(accessToken);

        String providerId = String.valueOf(userInfo.id());
        String email = getEmail(userInfo);
        String imageUrl = getProfileImage(userInfo);

        return issueTokens(OAuthProvider.KAKAO, providerId, email, imageUrl);
    }

    @Transactional
    public TokenIssueResult googleLogin(String code) {
        String accessToken = requestGoogleAccessToken(normalizeCode(code));
        GoogleUserInfo userInfo = requestGoogleUserInfo(accessToken);

        return issueTokens(
                OAuthProvider.GOOGLE,
                userInfo.sub(),
                userInfo.email(),
                userInfo.picture()
        );
    }

    @Transactional
    public TokenIssueResult reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(member);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member);

        return new TokenIssueResult(
                new TokenResponseDto(
                        newAccessToken,
                        "Bearer",
                        false,
                        MemberResponseDto.from(member)
                ),
                newRefreshToken
        );
    }

    private TokenIssueResult issueTokens(
            String provider,
            String providerId,
            String email,
            String imageUrl
    ) {
        MemberSearchResult searchResult =
                findOrCreateMember(provider, providerId, email, imageUrl);

        Member member = searchResult.member();

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);

        return new TokenIssueResult(
                new TokenResponseDto(
                        accessToken,
                        "Bearer",
                        searchResult.newMember(),
                        MemberResponseDto.from(member)
                ),
                refreshToken
        );
    }

    private String requestKakaoAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            body.add("client_secret", kakaoClientSecret);
        }

        KakaoTokenResponse response =
                webClient.post()
                        .uri("https://kauth.kakao.com/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(body))
                        .retrieve()
                        .bodyToMono(KakaoTokenResponse.class)
                        .block();

        if (response == null || response.accessToken() == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        return response.accessToken();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        if (!code.contains("%")) {
            return code;
        }

        return URLDecoder.decode(code, StandardCharsets.UTF_8);
    }

    private KakaoUserInfo requestKakaoUserInfo(String accessToken) {
        KakaoUserInfo response =
                webClient.get()
                        .uri("https://kapi.kakao.com/v2/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(KakaoUserInfo.class)
                        .block();

        if (response == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        return response;
    }

    private String requestGoogleAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("client_id", googleClientId);
        body.add("redirect_uri", googleRedirectUri);
        body.add("code", code);

        if (googleClientSecret != null && !googleClientSecret.isBlank()) {
            body.add("client_secret", googleClientSecret);
        }

        GoogleTokenResponse response =
                webClient.post()
                        .uri("https://oauth2.googleapis.com/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(body))
                        .retrieve()
                        .bodyToMono(GoogleTokenResponse.class)
                        .block();

        if (response == null || response.accessToken() == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        return response.accessToken();
    }

    private GoogleUserInfo requestGoogleUserInfo(String accessToken) {
        GoogleUserInfo response =
                webClient.get()
                        .uri("https://openidconnect.googleapis.com/v1/userinfo")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(GoogleUserInfo.class)
                        .block();

        if (response == null) {
            throw new CustomException(ErrorCode.OAUTH_PROVIDER_ERROR);
        }

        return response;
    }

    private MemberSearchResult findOrCreateMember(
            String provider,
            String providerId,
            String email,
            String imageUrl
    ) {
        return memberRepository
                .findByProviderAndProviderId(provider, providerId)
                .map(member -> {
                    member.updateOAuthInfo(email, imageUrl);
                    return new MemberSearchResult(member, false);
                })
                .orElseGet(() -> {
                    Member newMember =
                            OAuthProvider.KAKAO.equals(provider)
                                    ? Member.createKakaoMember(providerId, email, imageUrl)
                                    : Member.createGoogleMember(providerId, email, imageUrl);

                    return new MemberSearchResult(memberRepository.save(newMember), true);
                });
    }

    public long getRefreshTokenMaxAgeSeconds() {
        return jwtTokenProvider.getRefreshExpiration() / 1000;
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

        return userInfo.kakaoAccount().profile().profileImageUrl();
    }

    private record MemberSearchResult(
            Member member,
            boolean newMember
    ) {
    }

    public record TokenIssueResult(
            TokenResponseDto response,
            String refreshToken
    ) {
    }

    private record KakaoTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    private record KakaoUserInfo(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(
            String email,
            KakaoProfile profile
    ) {
    }

    private record KakaoProfile(
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {
    }

    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    private record GoogleUserInfo(
            String sub,
            String email,
            String picture
    ) {
    }
}
