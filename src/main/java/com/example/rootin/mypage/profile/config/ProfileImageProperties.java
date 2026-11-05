package com.example.rootin.mypage.profile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.profile-image")
public record ProfileImageProperties(
        String bucket,
        String region,
        String publicBaseUrl,
        String keyPrefix,
        long maxSize
) {
}
