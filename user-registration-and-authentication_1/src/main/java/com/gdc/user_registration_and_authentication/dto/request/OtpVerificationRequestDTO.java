package com.gdc.user_registration_and_authentication.dto.request;

import com.gdc.user_registration_and_authentication.enums.OtpType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationRequestDTO {

    private UUID userId;
    private String otp;
    private OtpType otpType;
}

