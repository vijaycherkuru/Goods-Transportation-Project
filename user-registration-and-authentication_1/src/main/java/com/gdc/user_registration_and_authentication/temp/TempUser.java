package com.gdc.user_registration_and_authentication.temp;

import com.gdc.user_registration_and_authentication.enums.Gender;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TempUser {
    private String email;
    private String otp;
    private Timestamp otpGeneratedAt;
    private boolean otpVerified;
}
