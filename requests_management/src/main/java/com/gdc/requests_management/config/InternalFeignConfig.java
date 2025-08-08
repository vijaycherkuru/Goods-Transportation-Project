package com.gdc.requests_management.config;

import com.gdc.requests_management.client.UserServiceClient;
import com.gdc.requests_management.dto.request.LoginRequestDTO;
import com.gdc.requests_management.dto.response.StandardResponse;
import com.gdc.requests_management.feign.dto.LoginResponseDTO;
import com.gdc.requests_management.feign.dto.UserResponseDTO;
import feign.RequestInterceptor;
import feign.hystrix.FallbackFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Configuration
@Slf4j
public class InternalFeignConfig {

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public RequestInterceptor internalFeignInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Internal-Key", internalApiKey);

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);

                    try {
                        String token = authHeader.substring(7);
                        Claims claims = Jwts.parser()
                                .setSigningKey(jwtSecret)
                                .parseClaimsJws(token)
                                .getBody();

                        String userId = claims.getSubject(); // UUID string
                        if (userId != null) {
                            requestTemplate.header("X-User-Id", userId);
                        }
                    } catch (Exception e) {
                        log.warn("❌ Unable to extract user ID from JWT: {}", e.getMessage());
                    }
                }
            }
        };
    }

    @Bean
    public FallbackFactory<UserServiceClient> userServiceClientFallbackFactory() {
        return cause -> new UserServiceClient() {
            @Override
            public LoginResponseDTO login(LoginRequestDTO request) {
                log.error("❌ Fallback: login failed due to: {}", cause.getMessage());
                throw new RuntimeException("User service login failed: " + cause.getMessage());
            }

            @Override
            public StandardResponse<UserResponseDTO> getUserDetails(UUID userId) {
                log.error("❌ Fallback: failed to fetch user details for {}: {}", userId, cause.getMessage());
                return StandardResponse.<UserResponseDTO>error(
                        503,
                        "User service is currently unavailable",
                        "/api/users/" + userId
                );
            }

            @Override
            public void banUser(UUID userId, String reason) {
                log.warn("⚠️ Fallback: banUser failed for {}: {}", userId, cause.getMessage());
            }
        };
    }
}
