package com.gdc.user_registration_and_authentication.controller;

import com.gdc.user_registration_and_authentication.dto.feign.UserResponseDTO;
import com.gdc.user_registration_and_authentication.dto.request.*;
import com.gdc.user_registration_and_authentication.dto.response.StandardResponse;
import com.gdc.user_registration_and_authentication.dto.response.UserRegistrationResponseDTO;
import com.gdc.user_registration_and_authentication.entity.User;
import com.gdc.user_registration_and_authentication.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    @PostMapping("/request-otp")
    public ResponseEntity<StandardResponse<String>> requestOtp(
            @RequestBody OtpMail otpMail){
        return ResponseEntity.ok(StandardResponse.ok(userService.requestOtp(otpMail.getEmail())));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<StandardResponse<String>> verifyOtp(
            @RequestBody VerifyOtpRequestDTO verifyOtpRequestDTO) {
        userService.verifyOtp(verifyOtpRequestDTO.getEmail(), verifyOtpRequestDTO.getOtp());
        return ResponseEntity.ok(StandardResponse.ok("OTP verified successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<UserRegistrationResponseDTO>> completeRegistration(
            @Valid @RequestBody UserRegistrationRequestDTO requestDTO) {
        return ResponseEntity.ok(StandardResponse.ok(userService.completeRegistration(requestDTO)));
    }

    // existing methods like getUser(), updateProfile(), etc.



    @GetMapping("/test")
    public ResponseEntity<StandardResponse<String>> test() {
        return ResponseEntity.ok(StandardResponse.ok("API is working"));
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<StandardResponse<User>> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequestDTO requestDTO) {
        return ResponseEntity.ok(StandardResponse.ok(userService.updateProfile(userId, requestDTO)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<StandardResponse<UserResponseDTO>> getUser(
            @PathVariable UUID userId,
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        String maskedAuth = (auth != null && auth.length() > 20)
                ? auth.substring(0, 10) + "..." + auth.substring(auth.length() - 5)
                : "null";

        String maskedInternalKey = (internalKey != null) ? "***MASKED***" : "null";

        System.out.println("🔒 Authorization: " + maskedAuth);
        System.out.println("🛡️  X-Internal-Key: " + maskedInternalKey);
        System.out.println("👤 X-User-Id: " + userIdHeader);

        User user = userService.getUserById(userId);
        UserResponseDTO dto = new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone());
        return ResponseEntity.ok(StandardResponse.ok(dto));
    }
}
