package com.gdc.user_registration_and_authentication.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordResponseDTO {
    private boolean success;
    private String message;
    private String email;
    private Integer otpExpiryMinutes;
}