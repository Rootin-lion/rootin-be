package com.example.rootin.member.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.config.OAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OAuthRedirectUriValidatorTest {

    private static final String LOCAL_KAKAO_REDIRECT_URI =
            "http://localhost:3000/oauth/kakao/callback";
    private static final String PROD_KAKAO_REDIRECT_URI =
            "https://rootin.example/oauth/kakao/callback";
    private static final String LOCAL_GOOGLE_REDIRECT_URI =
            "http://localhost:3000/oauth/google/callback";
    private static final String PROD_GOOGLE_REDIRECT_URI =
            "https://rootin.example/oauth/google/callback";

    private OAuthRedirectUriValidator validator;

    @BeforeEach
    void setUp() {
        OAuthProperties properties = new OAuthProperties(
                new OAuthProperties.RedirectUris(
                        Set.of(LOCAL_KAKAO_REDIRECT_URI, PROD_KAKAO_REDIRECT_URI),
                        Set.of(LOCAL_GOOGLE_REDIRECT_URI, PROD_GOOGLE_REDIRECT_URI)
                )
        );
        validator = new OAuthRedirectUriValidator(properties);
    }

    @Test
    void allowsRegisteredKakaoRedirectUri() {
        assertEquals(
                LOCAL_KAKAO_REDIRECT_URI,
                validator.validateKakao(LOCAL_KAKAO_REDIRECT_URI)
        );
    }

    @Test
    void allowsRegisteredGoogleRedirectUri() {
        assertEquals(
                PROD_GOOGLE_REDIRECT_URI,
                validator.validateGoogle(PROD_GOOGLE_REDIRECT_URI)
        );
    }

    @Test // 등록되지 않은 주소 거부
    void rejectsUnregisteredRedirectUri() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> validator.validateGoogle("https://rootin.example.evil.com/oauth/google/callback")
        );

        assertEquals(ErrorCode.INVALID_OAUTH_REDIRECT_URI, exception.getErrorCode()); // 올바른 에러 코드인지 확인
    }

    @Test // 다른 제공자의 URI 거부 ( 카카오 -X-> 구글 )
    void rejectsRedirectUriRegisteredForDifferentProvider() {
        CustomException exception = assertThrows(
                CustomException.class,
                () -> validator.validateGoogle(PROD_KAKAO_REDIRECT_URI)
        );

        assertEquals(ErrorCode.INVALID_OAUTH_REDIRECT_URI, exception.getErrorCode());
    }
}
