package com.fraud.fraud_detection.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ✅ Configure message broker
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println("WebSocket Broker Enabled ✅");

        // Clients will subscribe here
        config.enableSimpleBroker("/topic");

        // Prefix for client-to-server messages (not used now but important)
        config.setApplicationDestinationPrefixes("/app");
    }

    // ✅ Register WebSocket endpoint (NO SockJS)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("WebSocket Endpoint Registered ✅");

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");  // allow Angular frontend
    }
}