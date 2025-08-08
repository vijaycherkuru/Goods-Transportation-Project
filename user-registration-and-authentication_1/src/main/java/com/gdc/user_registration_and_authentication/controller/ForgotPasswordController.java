package com.gdc.user_registration_and_authentication.controller;

import com.gdc.user_registration_and_authentication.dto.request.ForgotPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.ResetPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.VerifyOtpRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.ForgotPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.VerifyOtpResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.ResetPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDTO> sendOtpToEmail(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        return ResponseEntity.ok(forgotPasswordService.sendOtpToEmail(request.getEmail()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponseDTO> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        log.info("OTP verification request for email: {}", request.getEmail());
        return ResponseEntity.ok(forgotPasswordService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        log.info("Password reset request for email: {}", request.getEmail());
        return ResponseEntity.ok(forgotPasswordService.resetPassword(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ForgotPasswordResponseDTO> resendOtp(@RequestBody ForgotPasswordRequestDTO request) {
        log.info("Resend OTP request for email: {}", request.getEmail());
        return ResponseEntity.ok(forgotPasswordService.resendOtp(request.getEmail()));
    }
}