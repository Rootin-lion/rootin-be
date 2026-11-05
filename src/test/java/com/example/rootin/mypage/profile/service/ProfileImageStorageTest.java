package com.example.rootin.mypage.profile.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.mypage.profile.config.ProfileImageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileImageStorageTest {

    private final S3Client s3Client = mock(S3Client.class);
    private final ProfileImageStorage storage = new ProfileImageStorage(
            s3Client,
            new ProfileImageProperties(
                    "rootin-images",
                    "ap-northeast-2",
                    "https://images.example.com",
                    "profile-images",
                    1024
            )
    );

    @Test
    void uploadsImageUsingDetectedType() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile image = new MockMultipartFile(
                "image", "photo.exe", "application/octet-stream", png
        );
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String imageUrl = storage.store(image);

        assertTrue(imageUrl.matches(
                "https://images\\.example\\.com/profile-images/[0-9a-f-]+\\.png"
        ));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsInvalidImageBytes() {
        MockMultipartFile image = new MockMultipartFile(
                "image", "fake.png", "image/png", "not an image".getBytes()
        );

        CustomException exception = assertThrows(CustomException.class, () -> storage.store(image));

        assertEquals(ErrorCode.INVALID_PROFILE_IMAGE, exception.getErrorCode());
    }

    @Test
    void rejectsImageOverConfiguredLimit() {
        ProfileImageStorage smallStorage = new ProfileImageStorage(
                s3Client,
                new ProfileImageProperties(
                        "rootin-images",
                        "ap-northeast-2",
                        "https://images.example.com",
                        "profile-images",
                        3
                )
        );
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", jpeg);

        CustomException exception = assertThrows(CustomException.class, () -> smallStorage.store(image));

        assertEquals(ErrorCode.PROFILE_IMAGE_TOO_LARGE, exception.getErrorCode());
    }

    @Test
    void deletesOnlyManagedProfileImage() {
        storage.deleteIfManaged(
                "https://images.example.com/profile-images/4f9ed91c-84dc-4a37-b9bb-a014ad55b650.png"
        );

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
