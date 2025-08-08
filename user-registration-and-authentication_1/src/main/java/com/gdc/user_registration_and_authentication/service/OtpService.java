package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.OtpRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.OtpVerificationRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.OtpVerificationResponseDTO;

public interface OtpService {
    OtpResponseDTO sendOtp(OtpRequestDTO requestDTO);
    OtpVerificationResponseDTO verifyOtp(OtpVerificationRequestDTO requestDTO);
}

