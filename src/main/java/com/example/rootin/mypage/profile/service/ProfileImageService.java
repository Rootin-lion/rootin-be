package com.example.rootin.mypage.profile.service;

import com.example.rootin.global.exception.CustomException;
import com.example.rootin.global.exception.ErrorCode;
import com.example.rootin.member.dto.response.MemberResponseDto;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final MemberRepository memberRepository;
    private final ProfileImageStorage profileImageStorage;

    @Transactional
    public MemberResponseDto updateProfileImage(Long memberId, MultipartFile image) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        String previousImageUrl = member.getImgUrl();
        String newImageUrl = profileImageStorage.store(image);
        member.updateProfileImage(newImageUrl);
        registerFileCleanup(previousImageUrl, newImageUrl);

        return MemberResponseDto.from(member);
    }

    private void registerFileCleanup(String previousImageUrl, String newImageUrl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                profileImageStorage.deleteIfManaged(previousImageUrl);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    profileImageStorage.deleteIfManaged(newImageUrl);
                }
            }
        });
    }
}
