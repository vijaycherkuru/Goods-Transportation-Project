package com.gdc.user_registration_and_authentication.dto.request;

import com.gdc.user_registration_and_authentication.enums.OtpType;
import lombok.Data;

@Data
public class OtpRequestDTO {
    private String email;
    private String phone;
    private OtpType otpType;
}
