package com.example.rootin.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

// YAML 설정을 Java에서 사용할 수 있도록 받음
@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(
        RedirectUris redirectUris
) {
    public record RedirectUris(
            Set<String> kakao,  // 중복없이 여러 주소 보관
            Set<String> google
    ) {
    }
}
