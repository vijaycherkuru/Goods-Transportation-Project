package com.gdc.user_registration_and_authentication.controller;

import com.gdc.user_registration_and_authentication.dto.request.OtpRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.OtpVerificationRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpVerificationResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.StandardResponse;
import com.gdc.user_registration_and_authentication.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public ResponseEntity<StandardResponse<OtpResponseDTO>> sendOtp(@RequestBody OtpRequestDTO request) {
        return ResponseEntity.ok(StandardResponse.ok(otpService.sendOtp(request)));
    }

    @PostMapping("/verify")
    public ResponseEntity<StandardResponse<OtpVerificationResponseDTO>> verifyOtp(@RequestBody OtpVerificationRequestDTO requestDTO) {
        return ResponseEntity.ok(StandardResponse.ok(otpService.verifyOtp(requestDTO)));
    }
}

