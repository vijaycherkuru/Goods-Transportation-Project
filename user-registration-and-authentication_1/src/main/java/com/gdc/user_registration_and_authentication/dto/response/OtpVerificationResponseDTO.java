package com.gdc.user_registration_and_authentication.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationResponseDTO {
    private String message;
}

