package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Looking up user by identifier: {}", identifier);

        Optional<User> userOptional;

        // Check if identifier is a UUID (userId from JWT)
        try {
            UUID userId = UUID.fromString(identifier);
            userOptional = userRepository.findById(userId);
        } catch (IllegalArgumentException e) {
            // Fallback to username/email/phone if not UUID
            userOptional = userRepository.findByUsernameOrEmailOrPhone(identifier, identifier, identifier);
        }

        User user = userOptional.orElseThrow(() -> {
            log.error("User not found with identifier: {}", identifier);
            return new UsernameNotFoundException("User not found: " + identifier);
        });

        log.debug("User found: {} with role: {}", user.getUsername());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString()) // Use userId here to match JWT's subject
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
