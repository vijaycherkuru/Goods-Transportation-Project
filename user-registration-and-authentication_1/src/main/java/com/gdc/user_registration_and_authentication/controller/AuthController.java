package com.gdc.user_registration_and_authentication.controller;

import com.gdc.user_registration_and_authentication.dto.request.LoginRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.UpdateProfileRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.*;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.exception.UserNotFoundException;
import com.gdc.user_registration_and_authentication.repository.OtpRepository;
import com.gdc.user_registration_and_authentication.repository.UserRepository;
import com.gdc.user_registration_and_authentication.service.AuthService;
import com.gdc.user_registration_and_authentication.util.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OtpRepository otpRepository;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("Login request received for: {}", request.getEmailOrPhone());
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(StandardResponse.ok(response));
    }

    @GetMapping("/profile")
    public ResponseEntity<StandardResponse<UserProfileResponseDTO>> getProfile(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.error("Unauthorized or invalid token"));
        }
        String userId = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername(); // This is userId from JWT
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserProfileResponseDTO profile = UserProfileResponseDTO.builder()
                .id(user.getId())
                .name(user.getUsername() != null ? user.getName() : user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .age(user.getAge())
                .gender(user.getGender())
                .street(user.getStreet())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .country(user.getCountry())
                .build();

        return ResponseEntity.ok(StandardResponse.ok(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<StandardResponse<UpdateProfileResponseDTO>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequestDTO requestDTO,
            Authentication authentication) {

        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Invalid or expired token"));
        }

        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardResponse.error("Unauthorized or invalid token"));
        }
        String userId = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername(); // This is userId from JWT
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (requestDTO.getName() != null) user.setName(requestDTO.getName());

        userRepository.save(user);

        UpdateProfileResponseDTO response = UpdateProfileResponseDTO.builder()
                .userId(user.getId())
                .message("Profile updated successfully")
                .build();

        return ResponseEntity.ok(StandardResponse.ok(response));
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<StandardResponse<String>> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Invalid or expired token"));
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UserNotFoundException("User not found")));


        otpRepository.deleteAllByUser(user);
        userRepository.delete(user);

        return ResponseEntity.ok(StandardResponse.ok("Account deleted successfully"));
    }
}

