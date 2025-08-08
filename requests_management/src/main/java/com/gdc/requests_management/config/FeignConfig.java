/*package com.gdc.requests_management.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Propagate Authorization header
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authHeader);
                }

                // Propagate Content-Type if needed
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");

                // Optionally propagate X-User-Id
                String userId = request.getHeader("X-User-Id");
                if (userId != null) {
                    requestTemplate.header("X-User-Id", userId);
                }
            }
        };
    }
}*/
