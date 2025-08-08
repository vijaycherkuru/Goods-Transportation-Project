package com.gdc.user_registration_and_authentication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOtpResponseDTO {
    private boolean success;
    private String message;
    // Removed email field for security - only return verification status
}