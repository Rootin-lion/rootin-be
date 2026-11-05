package com.example.rootin.mypage.profile.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.mypage.profile.config.ProfileImageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileImageStorage {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final S3Client s3Client;
    private final ProfileImageProperties properties;

    public String store(MultipartFile image) {
        validateBasic(image);
        String contentType = detectImageType(image);
        String key = normalizeKeyPrefix(properties.keyPrefix())
                + UUID.randomUUID() + EXTENSIONS.get(contentType);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(image.getSize())
                .build();

        try (InputStream inputStream = image.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, image.getSize()));
        } catch (IOException | SdkException e) {
            throw new CustomException(ErrorCode.PROFILE_IMAGE_STORAGE_ERROR);
        }

        return normalizeBaseUrl(properties.publicBaseUrl()) + "/" + key;
    }

    public void deleteIfManaged(String imageUrl) {
        String baseUrl = normalizeBaseUrl(properties.publicBaseUrl()) + "/";
        if (imageUrl == null || !imageUrl.startsWith(baseUrl)) {
            return;
        }

        String key = imageUrl.substring(baseUrl.length());
        if (key.isBlank() || !key.startsWith(normalizeKeyPrefix(properties.keyPrefix()))) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
        } catch (SdkException ignored) {
            // 새 이미지 반영은 성공했으므로 이전 객체 삭제 실패는 업로드 실패로 만들지 않는다.
        }
    }

    private void validateBasic(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PROFILE_IMAGE);
        }
        if (image.getSize() > properties.maxSize()) {
            throw new CustomException(ErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }
    }

    private String detectImageType(MultipartFile image) {
        byte[] header = new byte[12];
        try (InputStream inputStream = image.getInputStream()) {
            int length = inputStream.read(header);
            if (length >= 3 && unsigned(header[0]) == 0xFF
                    && unsigned(header[1]) == 0xD8 && unsigned(header[2]) == 0xFF) {
                return "image/jpeg";
            }
            if (length >= 8 && unsigned(header[0]) == 0x89 && header[1] == 'P'
                    && header[2] == 'N' && header[3] == 'G') {
                return "image/png";
            }
            if (length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F'
                    && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a') {
                return "image/gif";
            }
            if (length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F'
                    && header[3] == 'F' && header[8] == 'W' && header[9] == 'E'
                    && header[10] == 'B' && header[11] == 'P') {
                return "image/webp";
            }
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_PROFILE_IMAGE);
        }
        throw new CustomException(ErrorCode.INVALID_PROFILE_IMAGE);
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String normalizeKeyPrefix(String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "";
        }
        String normalized = keyPrefix.startsWith("/") ? keyPrefix.substring(1) : keyPrefix;
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }
}
