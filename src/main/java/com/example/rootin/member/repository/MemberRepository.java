package com.example.rootin.member.repository;

import com.example.rootin.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(String provider, String providerId);
    Optional<Member> findByRefreshToken(String refreshToken);
    boolean existsByNickname(String nickname);
}
