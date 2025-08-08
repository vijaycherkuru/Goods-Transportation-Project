package com.gdc.user_registration_and_authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class OtpResponseDTO {
    private String userId;
    private String message;

    // Optional: Only set this in debug mode
    private String otp;
}
