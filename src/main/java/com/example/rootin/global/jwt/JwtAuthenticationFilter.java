package com.example.rootin.global.jwt;

import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (
                token != null
                        && jwtTokenProvider.validateToken(token)
                        && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null
        ) {
            Long memberId =
                    jwtTokenProvider.getMemberId(token);

            memberRepository.findById(memberId)
                    .ifPresent(this::setAuthentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(
            HttpServletRequest request
    ) {
        String authorization =
                request.getHeader("Authorization");

        if (
                authorization == null
                        || !authorization.startsWith("Bearer ")
        ) {
            return null;
        }

        return authorization.substring(7);
    }

    private void setAuthentication(Member member) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        member.getId(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        member.getRole()
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}