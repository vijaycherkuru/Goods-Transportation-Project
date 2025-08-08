package com.gdc.ride_management.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;

@Configuration
@Slf4j
public class InternalFeignConfig {

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public RequestInterceptor internalRequestInterceptor() {
        return requestTemplate -> {
            // Set internal API key for service-to-service communication
            requestTemplate.header("X-Internal-Key", internalApiKey);

            // Access current HTTP request
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                // Get Authorization header
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);

                    String token = authHeader.substring(7); // remove "Bearer "

                    try {
                        // Decode Base64 secret and parse token
                        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));

                        Claims claims = Jwts.parserBuilder()
                                .setSigningKey(key)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

                        String userId = claims.getSubject(); // `sub` = userId
                        if (userId != null && !userId.isBlank()) {
                            requestTemplate.header("X-User-Id", userId);
                            log.debug("🔁 Extracted and set X-User-Id from JWT: {}", userId);
                        } else {
                            log.warn("⚠️ JWT subject (user ID) is missing or blank.");
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to parse JWT: {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ Authorization header missing or not a Bearer token.");
                }
            } else {
                log.warn("⚠️ No ServletRequestAttributes found. Feign call may be missing context.");
            }
        };
    }
}
