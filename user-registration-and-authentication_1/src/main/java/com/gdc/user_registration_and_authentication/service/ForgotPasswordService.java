package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.ResetPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.ForgotPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.ResetPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.PasswordResetFormResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.VerifyOtpResponseDTO;

public interface ForgotPasswordService {
    ForgotPasswordResponseDTO sendOtpToEmail(String email);
    VerifyOtpResponseDTO verifyOtp(String email, String otp);
    PasswordResetFormResponseDTO getPasswordResetForm(String email, String otp);
    ResetPasswordResponseDTO resetPassword(ResetPasswordRequestDTO request);
    ForgotPasswordResponseDTO resendOtp(String email);
}