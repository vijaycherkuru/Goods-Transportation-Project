package com.gdc.requests_management.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;
import java.util.Base64;

@Component
@Slf4j
public class EmailTokenValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key secretKey;

    @PostConstruct
    public void init() {
        // ✅ Fix: Use Base64 decoding for the JWT secret
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public void validateToken(String token, UUID requestId) {
        try {
            Claims claims = parseClaims(token);
            String tokenRequestId = claims.get("requestId", String.class);

            if (!requestId.toString().equals(tokenRequestId)) {
                throw new IllegalArgumentException("❌ Token requestId does not match URL requestId.");
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("❌ Invalid token: " + e.getMessage());
        }
    }

    public UUID extractRideUserId(String token) {
        Claims claims = parseClaims(token);
        return UUID.fromString(claims.get("rideUserId", String.class)); // ✅ updated key
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
