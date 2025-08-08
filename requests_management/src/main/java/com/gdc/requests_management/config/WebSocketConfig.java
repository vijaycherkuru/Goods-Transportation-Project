package com.gdc.requests_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for client connections (e.g., ws://localhost:8080/ws)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200") // ✅ specify allowed frontend origins
                .withSockJS(); // Optional: fallback for browsers not supporting WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixes for message routing
        registry.enableSimpleBroker("/topic", "/queue"); // ✅ Broker for subscribing messages
        registry.setApplicationDestinationPrefixes("/app"); // ✅ Prefix for sending from client to server
        registry.setUserDestinationPrefix("/user"); // ✅ Enables /user/{username}/... for private messaging
    }
}
