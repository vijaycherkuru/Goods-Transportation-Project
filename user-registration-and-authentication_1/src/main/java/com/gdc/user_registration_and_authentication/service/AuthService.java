package com.gdc.user_registration_and_authentication.service;

import com.gdc.user_registration_and_authentication.dto.request.ForgotPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.LoginRequestDTO;
import com.gdc.user_registration_and_authentication.dto.request.ResetPasswordRequestDTO;
import com.gdc.user_registration_and_authentication.dto.response.ForgotPasswordResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.LoginResponseDTO;
import com.gdc.user_registration_and_authentication.dto.response.ResetPasswordResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    ForgotPasswordResponseDTO forgotPassword(ForgotPasswordRequestDTO request);
    ResetPasswordResponseDTO resetPassword(ResetPasswordRequestDTO request);


}

