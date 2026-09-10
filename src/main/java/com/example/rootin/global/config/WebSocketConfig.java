package com.example.rootin.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtChannelInterceptor webSocketJwtChannelInterceptor;

    @Value("${app.websocket.endpoint}")
    private String endpoint;

    @Value("${app.websocket.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.websocket.application-prefix}")
    private String applicationPrefix;

    @Value("${app.websocket.broker-prefix}")
    private String brokerPrefix;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(applicationPrefix);
        registry.enableSimpleBroker(brokerPrefix);
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint).setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtChannelInterceptor);
    }
}
