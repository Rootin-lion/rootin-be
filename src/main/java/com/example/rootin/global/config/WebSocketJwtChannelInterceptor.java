package com.example.rootin.global.config;

import com.example.rootin.global.jwt.JwtTokenProvider;
import com.example.rootin.member.entity.Member;
import com.example.rootin.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebSocketJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                throw new IllegalArgumentException("Invalid or missing WebSocket access token.");
            }

            Long memberId = jwtTokenProvider.getMemberId(token);
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found."));
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    member.getId(),
                    null,
                    List.of(new SimpleGrantedAuthority(member.getRole().name()))
            ));
        }
        return message;
    }

    private String resolveToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }
}
