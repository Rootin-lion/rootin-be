package com.example.rootin.member.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.config.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuthRedirectUriValidator { // 허용 목록 검사

    private final OAuthProperties oauthProperties;

    public String validateKakao(String redirectUri) {
        return validate(redirectUri, oauthProperties.redirectUris().kakao());
    }

    public String validateGoogle(String redirectUri) {
        return validate(redirectUri, oauthProperties.redirectUris().google());
    }

    // 실제 허용 목록에 있는 주소만 허용
    private String validate(String redirectUri, Set<String> allowedRedirectUris) {
        if (redirectUri == null || !allowedRedirectUris.contains(redirectUri)) {
            throw new CustomException(ErrorCode.INVALID_OAUTH_REDIRECT_URI);
        }
        return redirectUri;
    }
}
