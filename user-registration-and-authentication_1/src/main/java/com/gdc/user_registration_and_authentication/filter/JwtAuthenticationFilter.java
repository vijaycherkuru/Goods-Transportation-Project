package com.gdc.user_registration_and_authentication.filter;

import com.gdc.user_registration_and_authentication.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${internal.api.key}")
    private String internalKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String internalHeader = request.getHeader("X-Internal-Key"); // ✅ Changed here

        // ✅ Internal request: bypass normal authentication
        if (internalHeader != null && internalHeader.equals(internalKey)) {
            log.debug("✅ Internal request accepted via X-Internal-Key for path: {}", path);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "internal-service", null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
            return;
        }

        // ✅ External request: check Bearer JWT
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("⚠️ Missing or invalid Authorization header for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;
        String role;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            log.debug("❌ Failed to parse JWT: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ Authenticated frontend user: {}", username);
                }
            } catch (Exception ex) {
                log.warn("⚠️ Fallback: Internal token authentication for {}", username);
                UsernamePasswordAuthenticationToken fallbackAuth =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                jwt
                        );
                fallbackAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(fallbackAuth);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean shouldSkip =
                path.equals("/api/users/register")
                        || path.equals("/api/users/test")
                        || path.matches("^/api/users/[a-fA-F0-9\\-]{36}$")
                        || path.startsWith("/actuator");

        if (shouldSkip) {
            log.debug("⏭️ Skipping JWT filter for path: {}", path);
        }

        return shouldSkip;
    }
}
